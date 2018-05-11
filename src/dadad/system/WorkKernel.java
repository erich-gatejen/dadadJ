package dadad.system;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import dadad.platform.AnnotatedException;
import dadad.platform.Context;
import dadad.platform.PropertyView;
import dadad.platform.config.Configurable;
import dadad.platform.config.ConfigurationType;
import dadad.platform.config.ContextConfiguration;
import dadad.platform.services.Logger;
import dadad.platform.services.LoggerLevel;
import dadad.platform.services.LoggerTarget;
import dadad.platform.services.PlatformInterfaceRequestor;
import dadad.system.api.APIDispatcher;

public class WorkKernel extends Thread implements Configurable {

	// ===============================================================================
	// = CLASS/ENUM
	
	public enum State {
		STARTING,
		ALIVE,
		REQUEST_HALT,
		HALTED;		
	}
	
	// ===============================================================================
	// = FIELDS
	
	public final static String SERVICECLASSPLY_NAME = ".name";
	public final static String SERVICECLASSPLY_CLASS = ".class";
	
	public final static long KERNEL_SNOOZE_MS = 500;
	public final static long KERNEL_HALT_SNOOZE_MS = 250;
	
	public final static String SYSLOG_FILE_NAME = "system.log";
	public final static String SYSLOG_KERNEL_TAG = "KERNEL";
	public final static String REPORT_FILE_PREFIX = "report.";
	public final static String REPORT_FILE_SUFFIX = ".log";
	
	// -- Allow package services see the kernel data
	
	Context rootContext;
	volatile HashMap<String, WorkProcess> processes;
	volatile HashMap<String, Logger> processesSystemLoggers;
	volatile HashMap<String, Logger> processesReportLoggers;
	volatile HashMap<String, LoggerTarget> processesReportTargets;
	APIDispatcher apiDispatcher;
	
	File logDirFile;
	Logger sysLogger;
	LoggerLevel logLevel;
	
	String kernelName = "WorkKernel";  // Hardcoded for now.
	
	static Object kernelMonitor = new Object();
	static WorkKernel kernel;
	
	private State state = State.STARTING;	
	private WorkKernelInterface kernelServices;
	
	// ===============================================================================
	// = OTHER INTERFACE
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{ SystemConfiguration.class };		
	}
	
	// ===============================================================================
	// = SERVICE
	
	public static SystemInterface getSystemInterface() {
		return kernel.kernelServices;
	}
	
	// ===============================================================================
	// = METHODS
	
	public WorkKernel(final Context rootContext) {
		super();
		this.rootContext = rootContext;
		processes = new HashMap<String, WorkProcess>(); 
		processesSystemLoggers = new HashMap<String, Logger>(); 
		processesReportLoggers = new HashMap<String, Logger>(); 
		processesReportTargets = new HashMap<String, LoggerTarget>(); 
		
		apiDispatcher = new APIDispatcher();
		apiDispatcher.configure(rootContext);
		
		// Eventually we will support more than one kernel, but for now just this guy.
		synchronized(kernelMonitor) {
			if (kernel != null) {
				throw new Error("BUG BUG BUG!!!  Only allowed one kernel for now.");
			}	
			kernel = this;
		}
		
		this.setName(kernelName);
		
		// Configured loglevel
		String logLevelText = rootContext.getConfig().getRequired(ContextConfiguration.CONTEXT_LOGLEVEL);
		try {
			logLevel = LoggerLevel.valueOf(logLevelText.trim());
		} catch (Exception e) {
			throw new AnnotatedException("Configured LogLevel is invalid", e)
				.annotate(ContextConfiguration.CONTEXT_LOGLEVEL.name(), logLevelText);
		}
		
		// Root system logger
		try {
			logDirFile = new File(rootContext.getRootPath(), rootContext.getConfig().getRequired(SystemConfiguration.LOG_DIRECTORY));
			if (! logDirFile.isDirectory()) throw new AnnotatedException("Log directory does not exist.")
				.annotate("path", logDirFile.getAbsolutePath());
			File sysLogFile = new File(logDirFile, SYSLOG_FILE_NAME);
			PrintWriter pw = new PrintWriter(new BufferedWriter(
					new FileWriter(sysLogFile, true)));
			sysLogger = new Logger(new LoggerTarget(pw, "file:/" + sysLogFile.getAbsolutePath()), SYSLOG_KERNEL_TAG).setLevel(logLevel);
			
		} catch (IOException ioe) {
			throw new AnnotatedException("Could not open system log file")
				.annotate("path", new File(logDirFile, SYSLOG_FILE_NAME));
		}
		
		kernelServices = new WorkKernelInterface(this);
		PlatformInterfaceRequestor.setPlatformInterface(kernelServices);
			
		sysLogger.info("Kernel [" + kernelName + "] built");
	}
	
	public synchronized String startWorkProcess(final String workProcessClassName, final String name, final Context context) {
		String result;
		try {
			if (state == State.REQUEST_HALT) throw new AnnotatedException("Cannot start process.  System is shutting down."); 
			if (processes.containsKey(name)) throw new AnnotatedException("Process already named this.");
			
			try {
				
				// Get object
				Class<?> clazz = Class.forName(workProcessClassName);
				WorkProcessContainer wpc = (WorkProcessContainer) clazz.newInstance();
				
				// Build context - For now, hardcode as DataContext, but we will change this later.
				WorkProcess workProcess = new WorkProcess(kernelServices, context, wpc, name);
				processes.put(workProcess.getName(), workProcess);

				workProcess.start();
				
				result = workProcess.getName();			
				
			// TODO: Cull bad processes
				
			} catch (InstantiationException ie) {
				throw new AnnotatedException("Failed to start work process because it failed instantiation.", AnnotatedException.Catagory.FAULT, ie);					
			} catch (ClassCastException cce) {	
				throw new AnnotatedException("Failed to start work process because implementation is not a WorkProcessContainer.", AnnotatedException.Catagory.FAULT);	
			} catch (ClassNotFoundException cnfe) {
				throw new AnnotatedException("Failed to start work process because implementation class does not exist.", AnnotatedException.Catagory.FAULT);	
			} catch (Exception e) {
				throw new AnnotatedException("Failed to start work process due to spurious exception.", AnnotatedException.Catagory.FAULT, e);		
			}
		} catch (AnnotatedException ae) {
			throw ae.annotate("workprocess.class.name", workProcessClassName);
		}
		return result;
	}
	
	public void reportWorkProcessEnd(final String name) {

	}
	
	public synchronized void requestStop() {
		state = State.REQUEST_HALT;
	}
	
	
	// ===============================================================================
	// = INTERNAL
	
	public void run() {

		sysLogger.info("Kernel [" + kernelName + "] starting.");
			
		String name = "[NONE]";
		try {
			
			PropertyView services = rootContext.copyProperties();
			for (String serviceConfig : services.ply(SystemConfiguration.SERVICE_CLASS_LIST.property())) {
				name = rootContext.getConfig().getRequired(serviceConfig + SERVICECLASSPLY_NAME);
				String clazz = rootContext.getConfig().getRequired(serviceConfig + SERVICECLASSPLY_CLASS);
				startWorkProcess(clazz, name, rootContext);
				sysLogger.info("Started process as a service.", "service.name", name, "service.class", clazz);
			}
			
			state = State.ALIVE;
			sysLogger.info("Kernel [" + kernelName + "] running.");
			
		} catch (Exception e) {
			sysLogger.fault("Kernel [" + kernelName + "] start failed because service start failed.", "service.name", name, "exception", AnnotatedException.render(e,true));
			state = State.HALTED;
			return;
		}
				
		while (state != State.HALTED) {
						
			try {
				Thread.sleep(KERNEL_SNOOZE_MS);
				
				if (state == State.REQUEST_HALT) {
					sysLogger.info("Kernel [" + kernelName + "] halt requested.  Shutting down.");
					
					// Not managing any dependencies.  Hopefully there are none.
					for (WorkProcess process : processes.values()) {
						sysLogger.debug("Kernel [" + kernelName + "] halting process " + process.getName());
						if (! process.getInfo().state.isTerminal()) process.halt();
					}
					
					for (WorkProcess process : processes.values()) {
						sysLogger.debug("Kernel [" + kernelName + "] waiting for process " + process.getName() + " to halt.");
						long startWait = System.currentTimeMillis();
						while (! process.getInfo().state.isTerminal()) {
							try {
								Thread.sleep(KERNEL_HALT_SNOOZE_MS);
								sysLogger.debug("Kernel [" + kernelName + "] waiting for process " + process.getName() + " to halt.",
										"wait.time", System.currentTimeMillis() - startWait, "state", process.getInfo().state);
							} catch (InterruptedException iiee) {
								// Ignore
								Thread.interrupted();								
							}	
						}
					}
					
					state = State.HALTED;
									
				} // end if halt
				
			} catch (InterruptedException ie) {
				// Ignore
				Thread.interrupted();
				
			} catch (Exception e) {
				sysLogger.fault("Kernel stopping due to spurious exception.  Forcing system exit.", e);
				System.exit(99);
			}
		}
		
		sysLogger.info("Kernel [" + kernelName + "] shut down.");
	}

	
}
