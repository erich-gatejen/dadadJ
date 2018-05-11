package dadad.system;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import dadad.platform.AnnotatedException;
import dadad.platform.Context;
import dadad.platform.config.ContextConfiguration;
import dadad.platform.services.Logger;
import dadad.platform.services.LoggerLevel;
import dadad.platform.services.LoggerTarget;
import dadad.system.api.APIDispatcher;

/**
 * Implements the system interface to the kernel.
 * 
 * IMPORTANT: All process management must be handled by the kernel itself and not here.  These methods will
 * call a kernel method.  Consider this class and the kernel as best buddies and manage the interface between the
 * two very carefully.
 * 
 */
public class WorkKernelInterface implements SystemInterface {

	// ===============================================================================
	// = FIELDS
	
	private final WorkKernel workKernel;
	
	// ===============================================================================
	// = SYSTEM INTERFACE
	
	public WorkKernelInterface(final WorkKernel workKernel) {
		this.workKernel = workKernel;
	}
	
	public Logger getReportLogger(final Object tag) {
		String name = Thread.currentThread().getName();
		Logger result = workKernel.processesReportLoggers.get(name);
		if (result == null) {
			
			try {
				File logFileFile = new File(workKernel.logDirFile, WorkKernel.REPORT_FILE_PREFIX + name + WorkKernel.REPORT_FILE_SUFFIX);
				// TODO might break if path is relative and does not start with a slash
				String logFileFileUrl = "file:/" + logFileFile.getAbsolutePath();
							
				PrintWriter pw = new PrintWriter(new BufferedWriter(
						new FileWriter(logFileFile, true)));
				LoggerTarget target = new LoggerTarget(pw, logFileFileUrl);
				workKernel.processesReportTargets.put(name, target);
								
				result = new Logger(target, tag.toString()).setLevel(workKernel.sysLogger.getLevel());
				
			} catch (IOException ioe) {
				throw new AnnotatedException("Could not open report log file")
					.annotate("path", new File(workKernel.logDirFile, WorkKernel.SYSLOG_FILE_NAME));
			}
			workKernel.processesReportLoggers.put(name, result);
			
		} else {
			result = result.getFriend(tag);
			
		}
		
		WorkProcess wp = workKernel.processes.get(name);
		result.setLevel(LoggerLevel.valueOf(wp.getContext().getConfig().getRequired(ContextConfiguration.CONTEXT_LOGLEVEL)));
		
		return result;
	}
	
	public Logger getLogger() {
		String name = Thread.currentThread().getName();
		Logger result = workKernel.processesSystemLoggers.get(name);
		if (result == null) {
			result = workKernel.sysLogger.getFriend(name);
			workKernel.processesSystemLoggers.put(name, result);
		}
		
		WorkProcess wp = workKernel.processes.get(name);
		result.setLevel(LoggerLevel.valueOf(wp.getContext().getConfig().getRequired(ContextConfiguration.CONTEXT_LOGLEVEL)));
		
		return result;
	}
	
	public Context getContext() {
		String name = Thread.currentThread().getName();
		WorkProcess wp = workKernel.processes.get(name);
		return wp.getContext();
	}
	
	public synchronized String startWorkProcess(final String workProcessClassName, final Context context) {
		return workKernel.startWorkProcess(workProcessClassName, null, context);		
	}
	
	public void reportWorkProcessEnding() {
		String name = Thread.currentThread().getName();
		LoggerTarget target = workKernel.processesReportTargets.get(name);
		if (target != null) {
			target.close();
		}
	}
	
	public APIDispatcher getAPIDispatcher() {
		return workKernel.apiDispatcher;
	}
	
	public void yieldToSystem() {
		String name = Thread.currentThread().getName();
		WorkProcess wp = workKernel.processes.get(name);
		wp.doYield();
	}
	
	public Context getNewContext() {
		return workKernel.rootContext.copyContext();
	}
	
	public Context getRootContext() {
		return workKernel.rootContext;
	}
	
	public WorkProcess getWorkProcess(final String name) {
		WorkProcess wp = workKernel.processes.get(name);
		if (wp == null) throw new AnnotatedException("Process does not exist").annotate("process.name", name);
		return wp;
	}
	
	public HashMap<String, WorkProcess> getProcessList() {
		return workKernel.processes;
	}
	
	public void requestStop() {
		// For now no protection, but this should be limited to certain processes, such as the SYSTEM api.
		workKernel.requestStop();		
	}
	
	public Logger getReportLoggerByName(final String workProcessName) {
		return workKernel.processesReportLoggers.get(workProcessName);
	}
}
