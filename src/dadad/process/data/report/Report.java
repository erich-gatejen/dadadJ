package dadad.process.data.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.logging.Logger;

import dadad.data.DataContext;
import dadad.platform.AnnotatedException;

import static dadad.platform.AnnotatedException.Catagory.FAULT;
import static dadad.platform.AnnotatedException.Catagory.ERROR;

/**
 * Start making it configurable.
 */
public abstract class Report {	
	
	// ===============================================================================
	// = FIELDS
	
	private Logger reportLogger = Logger.getLogger(DataContext.LOGGER__REPORTING);
	
	// ===============================================================================
	// = ABSTRACT

	abstract protected String reportName();
	
	abstract protected String reportFileName();
	
	abstract protected void runReport(final ReportContext reportContext, final BufferedWriter bw) throws Exception ;
	
	// ===============================================================================
	// = METHODS
	
	/**
	 * Do the report.
	 * @param reportContext
	 * @param reportBaseFile If a file, it will use the file.  If a directory, it will create a file in the directory with the name it gets from reportFileName()/
	 */
	public void report(final ReportContext reportContext, final File reportBaseFile) {
		
		File reportFile = reportBaseFile;
		if (reportFile.isDirectory()) {
			reportFile = new File(reportFile, reportFileName());			
		}
		
		BufferedWriter bw;
	    try {
	    	bw = new BufferedWriter(new FileWriter(reportFile));
	    } catch (Exception e) {
	    	throw new AnnotatedException("Could not open document frequency report file.", FAULT,  e)
	    			.annotate("report.name", reportName(), "report.file", reportFile.getAbsolutePath());
	    }
	  
	    try {
	    	runReport(reportContext, bw);
	    	
	    } catch (Exception e) {
	    	throw new AnnotatedException("Report failed.", ERROR, e)
				.annotate("report.name", reportName(), "report.file", reportFile.getAbsolutePath());  	
	    
	    } finally {
	    	try {
	    		bw.close();
	    	} catch (Exception e) {
	    		// Don't care
	    	}
	    }
		
	    reportLogger.info("Report done for " + reportName());
	}

	
}
