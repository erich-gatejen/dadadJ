package dadad.system;

import java.util.HashMap;

import dadad.platform.Context;
import dadad.platform.services.Logger;
import dadad.platform.services.PlatformInterface;
import dadad.system.api.APIDispatcher;

/**
 * System interface.
 * 
 * These are all equally privileged.
 * @author egatejen
 *
 */
public interface SystemInterface extends PlatformInterface {

	public APIDispatcher getAPIDispatcher();
	
	public String startWorkProcess(final String workProcessClassName, final Context context);
	
	public void yieldToSystem();
	
	/**
	 * Get a fresh and disassociated copy of the root context.  Be very careful playing with the root context.
	 * For the vast majority of cases you should use getContext() so you get any state the context has built since it's 
	 * birth.
	 * @return
	 */
	public Context getNewContext();
	
	public Context getRootContext();
	
	public WorkProcess getWorkProcess(final String name);
	
	public void reportWorkProcessEnding();
	
	public HashMap<String, WorkProcess> getProcessList();
	
	public void requestStop();
	
	/**
	 * Get a report logger by the work process name.  
	 * @param workProcessName
	 * @return the logger or null if the logger does not exist (meaning the process never asked to use it).
	 */
	public Logger getReportLoggerByName(final String workProcessName);
	
}
