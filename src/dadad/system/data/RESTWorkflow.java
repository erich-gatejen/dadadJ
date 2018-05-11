package dadad.system.data;


import dadad.data.config.WorkflowConfiguration;
import dadad.data.model.Result;
import dadad.platform.Context;
import dadad.platform.config.Configurable;
import dadad.platform.config.ConfigurationType;
import dadad.system.SystemConfiguration;
import dadad.system.WorkKernel;
import dadad.system.WorkProcessContainer;
import dadad.system.api.http.HttpServer;

/**
 * Configurable workflow.
 */
public class RESTWorkflow implements WorkProcessContainer, Configurable {	
	
	// ===============================================================================
	// = FIELDS
	
	public final static int SERVER_LISTEN_TIMEOUT = 500;
	public final static int SERVER_SOCKET_TIMEOUT = 60000;

	
	protected Context context;
	private HttpServer httpServer;
	
	// ===============================================================================
	// = METHODS
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{ WorkflowConfiguration.class, SystemConfiguration.class };
		
	}
	
	public RESTWorkflow() {
	}
	
	public void configure() {		
		context = WorkKernel.getSystemInterface().getContext();
		
		// data type for this context branch with be 
		
		httpServer = new HttpServer();
		httpServer.setup(context.getConfig().getBoundedInt(SystemConfiguration.REST_PORT, 1, 0xFFFF),
				SERVER_LISTEN_TIMEOUT);
	}
	
	public Result getCurrentResult() {
		return Result.inconclusive("REST");
	}
	
	public Result run() {
		
		while(true) {
			httpServer.service(SERVER_SOCKET_TIMEOUT);
			context.yield();
		}	
		
		// return Result.pass("REST");
	}

	public void close() {
		httpServer.close();
	}
	
	
	// ===============================================================================
	// = INTERNAL	
	
	
}
