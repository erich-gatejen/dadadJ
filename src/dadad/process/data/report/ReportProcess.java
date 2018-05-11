package dadad.process.data.report;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dadad.data.DataContext;
import dadad.data.config.ReportConfiguration;
import dadad.data.model.Document;
import dadad.data.store.IndexTermStoreReader;
import dadad.data.store.DocumentCatalogStore;
import dadad.data.store.Terms2BlockStore;
import dadad.platform.AnnotatedException;

import static dadad.platform.AnnotatedException.Catagory.FAULT;

/**
 * Report process.
 */
public class ReportProcess {	
	
	// ===============================================================================
	// = FIELDS

	private final DataContext context;
	
	private DocumentCatalogStore sourceStore;
	private IndexTermStoreReader elementStore;
	private Terms2BlockStore terms2DocStore;
	
	// ===============================================================================
	// = METHODS
	
	public ReportProcess(final DataContext context) {
		this.context = context;	
	}
	
	public void configure() {
	
		// Assume these are always needed for now.  This will be configurable later.
		if (sourceStore == null) {
			sourceStore = new DocumentCatalogStore(context);
		}
		if (elementStore == null) {
			elementStore = new IndexTermStoreReader(context);
			elementStore.open();			
		}
		if (terms2DocStore == null) {
			terms2DocStore = new Terms2BlockStore(context);			
		}
	}
	
	public void report(final String baseReportFilePath) {
		report(new File(baseReportFilePath));		
	}
	
	public void report(final File baseReportFilePath) {
	
		if (baseReportFilePath.isFile()) {
			File baseReportDirectory = baseReportFilePath.getParentFile();
			if ( (! baseReportDirectory.isDirectory()) && (! baseReportDirectory.mkdirs()) ) {
				throw new AnnotatedException("Could not create directory for reports.", FAULT)
					.annotate("base.file", baseReportFilePath);
			}
		}
		
		// --- Get sources -- only taking the first source for now.
		String[] configuredSources = context.getConfig().getMultivalue(ReportConfiguration.SOURCES);
		if ((configuredSources == null) || (configuredSources[0].length() < 1)) 
			ReportConfiguration.SOURCES.throwConfigException("Sources not configured");
		
		Document source = sourceStore.get(configuredSources[0]);
		if (source == null) throw new AnnotatedException("Document not found in source store", AnnotatedException.Catagory.FAULT)
			.annotate("configured.source", configuredSources[0]);
		
			
		// --- Get report classes
		List<Class<Report>> reportClasses = new LinkedList<Class<Report>>();
		Map<String, String[]> reportPly = context.getConfig().getPly(ReportConfiguration.REPORTS);
		for (String[] report : reportPly.values()) {
			try {				
				@SuppressWarnings("unchecked")
				Class<Report> clazz = (Class<Report>) Class.forName(report[0]);
				reportClasses.add(clazz);
				
			} catch (Exception e) {
				throw new AnnotatedException("Failed to get class for configured report.", FAULT, e)
					.annotate("class.name", report[0]);
			}
		}
		if (reportClasses.size() < 1) ReportConfiguration.REPORTS.throwConfigException("Report classes not configured");		
		
		// -- Context
		ReportContext reportContext = new ReportContext(context, source, elementStore, terms2DocStore);
				
		// -- Execute
		for (Class<Report> reportClass : reportClasses) {
			try {
				Report report = reportClass.newInstance(); 
				report.report(reportContext, baseReportFilePath);
				
			} catch (InstantiationException | IllegalAccessException e) {
				throw new AnnotatedException("Could not instantiate report class", FAULT, e) 
				.annotate("class.name", reportClass.getName());
			}
		}
	}	
	
	public void end() {
		
		try {
			elementStore.close();
		} catch (Throwable  t) {
			// Dont care
		}
		try {
			sourceStore.close();
		} catch (Throwable t) {
			// Dont care
		}
		try {
			terms2DocStore.close();
		} catch (Throwable t) {
			// Dont care
		}

	}
	
	protected void finalize() throws Throwable {
	     try {
	         end();
	     } finally {
	         super.finalize();
	     }
	 }

}
