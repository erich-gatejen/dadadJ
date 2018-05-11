package dadad.system.data.boot;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import dadad.data.DataContext;
import dadad.data.config.ListerConfiguration;
import dadad.data.config.ReportConfiguration;
import dadad.platform.AnnotatedException;
import dadad.platform.PropertyInjector;
import dadad.platform.PropertyStore;
import dadad.platform.PropertyView;
import dadad.platform.Util;
import dadad.platform.config.ContextConfiguration;
import dadad.process.data.report.ReportProcess;
import dadad.system.data.AnalysisWorkflow;
import dadad.system.data.ListerWorkflow;
import dadad.system.data.Workflow;

import static dadad.platform.config.ContextConfiguration.CONTEXT_ROOT_PATH;
import static dadad.platform.config.ContextConfiguration.CONTEXT_SOURCE;
import static dadad.platform.config.ContextConfiguration.CONTEXT_TARGET;
import static dadad.platform.config.ContextConfiguration.CONTEXT_RUN;

public class Bootstrap {
	
	public final static int ARG__COMMAND = 0;
	public final static int ARG__CONFIG_FILE = 1;
	
	private final DataContext rootContext;
	private final PropertyView rootProperties;
	
	static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tc %2$s %4$s: %5$s%6$s%n");
	}

	public Bootstrap(final DataContext rootContext, final PropertyView rootProperties) {
		this.rootContext = rootContext;
		this.rootProperties = rootProperties;
		
		// Logging
	    Handler logHandler = new StreamHandlerFlushing(System.out, new SimpleFormatter());
	    logHandler.setLevel(Level.FINE);
	    Logger sysLogger = Logger.getLogger(DataContext.LOGGER__SYSTEM);
	    sysLogger.addHandler(logHandler);
	    sysLogger.setLevel(Level.FINE);
	    Logger reportLogger = Logger.getLogger(DataContext.LOGGER__REPORTING);
	    reportLogger.addHandler(logHandler);
	    reportLogger.setLevel(Level.FINE);
	}
	
	public void run(final Command command) {
		
		Workflow workflow = null;
		
		String sourcePath = rootContext.getConfig().getRequired(CONTEXT_SOURCE);
		String targetPath = rootContext.getConfig().get(CONTEXT_TARGET);
		File rootPathFile = new File(rootContext.getRootPath());
		if (! rootPathFile.isDirectory()) throw new AnnotatedException("Root path not a directory")
			.annotate(CONTEXT_ROOT_PATH.property(), rootContext.getRootPath());

		
		switch (command) {
		case ANALYSIS:
		case REPORT:
		case BOTH:
			if (targetPath == null) {
				targetPath = sourcePath;
				rootContext.getConfig().set(CONTEXT_TARGET, sourcePath);
			}
			workflow = new AnalysisWorkflow(rootContext);
			break;
			
		case WF:
			String wfClassName = rootContext.getConfig().get(ContextConfiguration.CONTEXT_WORKFLOW_CLASS);
			if ((wfClassName == null) || (wfClassName.trim().length() < 1))
				throw new AnnotatedException("You must set the workflow class.")
					.annotate("property.name", ContextConfiguration.CONTEXT_WORKFLOW_CLASS.property());
				
			try {
				Class<?> clazz = Class.forName(wfClassName);
				workflow = (Workflow) clazz.getConstructor(DataContext.class).newInstance(rootContext);
				
			} catch (Exception e) {
				throw new AnnotatedException("Could not load the workflow class", AnnotatedException.Catagory.FAULT, e)
					.annotate("property.name", ContextConfiguration.CONTEXT_WORKFLOW_CLASS.property(), "class.name", wfClassName);
			}
			break;
			
		case LIST:
			if (! rootContext.getConfig().exists(ListerConfiguration.LIST_FILE)) {
				if (targetPath == null) throw new RuntimeException("Cannot determine the target lister file.  Set " + 
						ListerConfiguration.LIST_FILE.property());
				rootContext.getConfig().set(ListerConfiguration.LIST_FILE, targetPath);
			}
			
			workflow = new ListerWorkflow(rootContext);
			break;
		}
		
		switch (command) {
		case ANALYSIS:
		case LIST:
		case WF:
			workflow(workflow, sourcePath);
			break;
			
		case REPORT:
			report(targetPath, sourcePath);
			break;
			
		case BOTH:
			// Delete anything in the temp directory
			Util.cleanDirectory(rootPathFile);
			
			String targetDecoratedName = "report/";
			String run = rootContext.getConfig().get(CONTEXT_RUN);
			if ((run != null) && (run.trim().length() > 0)) targetDecoratedName = run + '-' + targetDecoratedName;		
			
			File targetFile = new File(targetPath);
			File targetContainterDir;
			if (targetFile.isDirectory()) {
				targetContainterDir = new File(targetFile.getAbsolutePath() + "/" + targetDecoratedName);		
			} else {
				targetContainterDir = new File(targetFile.getAbsolutePath() + "." + targetDecoratedName);									
			}
			
			if (targetContainterDir.isDirectory()) {
				Util.cleanDirectory(targetContainterDir);
			} else if (targetContainterDir.isFile()) {
				throw new AnnotatedException("Report directory path points to an existing file.")
					.annotate("report.directory", targetContainterDir.getAbsolutePath());				
			} else {
				if (! targetContainterDir.mkdirs()) throw new AnnotatedException("Failed to make report directory")
					.annotate("report.directory", targetContainterDir.getAbsolutePath());
			}
			
			workflow(workflow, sourcePath);			
			report(targetContainterDir.getAbsolutePath(), sourcePath);	
			
		}

	}
	
	public void workflow(final Workflow workflow, final String path) {
		workflow.configure();
		workflow.process(path);
		workflow.close();		
	}
	
	public void report(final String reportPath, final String sourcePath) {
		
		// Hotwire the sources.
		if ((sourcePath != null) && (sourcePath.trim().length() > 0)) {
			rootProperties.set(ReportConfiguration.SOURCES.property(), sourcePath);
		}
		
		ReportProcess reportProcess = new ReportProcess(rootContext);
		reportProcess.configure();
		reportProcess.report(reportPath);
		reportProcess.end();		
	}
	
	public static void main(String [] args) {
		
		try {
			if (args.length < 1) {
				throw new Exception("You must specify a command.");
			}
			if (args.length < 2) {
				throw new Exception("You must give the path to the configuration properties file.");
			}
			
			PropertyView properties = new PropertyStore().load(new File(args[ARG__CONFIG_FILE].trim()));			
			if (args.length > 2) {
				PropertyInjector.inject(properties, Arrays.copyOfRange(args, 2, args.length));
			}
			
			Bootstrap bootstrap = new Bootstrap(new DataContext(properties), properties);			
			bootstrap.run(Command.match(args[ARG__COMMAND]));
						
			System.out.println("Done.");
			
		} catch (Exception e) {
			System.out.println(AnnotatedException.render(e, true));
			System.exit(1);
		}
		
		System.exit(0);
	}
	
}
