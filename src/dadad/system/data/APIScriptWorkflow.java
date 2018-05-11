package dadad.system.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import dadad.data.config.APIConfiguration;
import dadad.data.config.WorkflowConfiguration;
import dadad.data.model.Result;
import dadad.platform.AnnotatedException;
import dadad.platform.Context;
import dadad.platform.Resolver;
import dadad.platform.config.Configurable;
import dadad.platform.config.ConfigurationType;
import dadad.platform.config.ContextConfiguration;
import dadad.platform.services.Logger;
import dadad.system.SystemInterface;
import dadad.system.WorkKernel;
import dadad.system.WorkProcessContainer;
import dadad.system.api.APIDispatcher;
import dadad.system.api.APIResponse;

/**
 * Configurable workflow.
 */
public class APIScriptWorkflow implements WorkProcessContainer, Configurable {	
	
	// ===============================================================================
	// = FIELDS
	
	public final static char COMMENT_CHARACTER = '#';
	
	protected Context context;
	private BufferedReader br;
	
	private APIDispatcher dispatcher;
	private Resolver resolver;
	
	private Logger sysLogger;
	
	private Result currentResult;
	
	// ===============================================================================
	// = METHODS
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{ WorkflowConfiguration.class, APIConfiguration.class };
		
	}
	
	public APIScriptWorkflow() {
		currentResult = Result.newResult("Data Workflow genesis.");
	}
	
	public void configure() {
		SystemInterface si = WorkKernel.getSystemInterface();
		context = si.getContext();
		dispatcher = si.getAPIDispatcher();
		resolver = context.getResolver();
		
		String script = "UNKNOWN SCRIPT";
		try {
			script = context.getConfig().getRequired(APIConfiguration.API_SCRIPT);
			if (script.trim().length() < 1) throw new AnnotatedException("Script is empty.")
				.annotate("property", APIConfiguration.API_SCRIPT.name());
			br = new BufferedReader(new StringReader(script));
						
			sysLogger = si.getLogger();
		} catch (Throwable t) {
			currentResult = Result.fault("APIScript: " + si.getContext().getConfig().get(ContextConfiguration.CONTEXT_RUN)
					+ " : " + script, t);
			throw t;
		}
		currentResult = Result.inconclusive("APIScrpit: " + si.getContext().getConfig().get(ContextConfiguration.CONTEXT_RUN)
				+ " : " + script);
	}
		
	public Result getCurrentResult() {
		return currentResult;
	}
	
	public Result run() {
		try {
			go();
			currentResult = Result.pass(currentResult.name);
		
		} catch (Throwable t) {
			currentResult = Result.fault(currentResult.name, t);
			throw t;
		}
		
		return currentResult;	
	}
	
	private void go() {
		
		try {
			
			sysLogger.info("Starting API script");
			String currentLine = br.readLine();
			int lineNumber = 1;
			while(currentLine != null) {
	
				Map<String, String> nv = new HashMap<String, String>();
				String path;
				
				try {
					currentLine = currentLine.trim();
					if ((currentLine.length() > 0) && (currentLine.charAt(0) != COMMENT_CHARACTER)) {
	
						currentLine = resolver.resolve(currentLine);
						
						try {
							
				            int pathDelim = currentLine.indexOf('?');
				            if (pathDelim >= 0) {
				                decodeNV(currentLine.substring(pathDelim + 1), nv);
				                path = URLDecoder.decode(currentLine.substring(0, pathDelim), "UTF8");
				            } else {
				                path = URLDecoder.decode(currentLine, "UTF8");
				            }
						
						} catch (Exception e) {
							throw new AnnotatedException("Failed to decode request.  Script halting.", AnnotatedException.Catagory.ERROR, e);				
						}
						
						APIResponse response;
						try {
							response = dispatcher.dispatcher(dispatcher.parse(path), nv);
							
						} catch (Exception ee) {
							throw new AnnotatedException("API call failed due to spurious exception.  Script halting.", AnnotatedException.Catagory.ERROR, ee);				
						}
						
						if (response.code.isFailure()) {
							if (response.exception == null)
								throw new AnnotatedException("API call failed.  Script halting.", AnnotatedException.Catagory.ERROR)
									.annotate("code", response.code.name());	
							else
								throw new AnnotatedException("API call failed.  Script halting.", AnnotatedException.Catagory.ERROR, response.exception)
									.annotate("code", response.code.name());	
							
						} else {
							if (sysLogger.isDebugging()) sysLogger.debug("Script request completed", "line.number", lineNumber, "request", currentLine);
						}
					
					} // end if line usable
				
				} catch (AnnotatedException ae) {
					throw ae.annotate("line.number", lineNumber, "request", currentLine);
				}
				
				context.yield();
				currentLine = br.readLine();
				lineNumber++;
			}	
			
		} catch (IOException ioe) {
			throw new AnnotatedException("IO exception while trying to read the script.", AnnotatedException.Catagory.FAULT, ioe);	
		}
		
		sysLogger.info("... API script complete.");
	}

	public void close() {
		try {
			br.close();
		} catch (Exception e) {
			// Don't care
		}
	}
	
	
	// ===============================================================================
	// = INTERNAL	
	
    private void decodeNV(String text, Map<String, String> nv) throws Exception {
        StringTokenizer tokanizer = new StringTokenizer(text, "&");

        while (tokanizer.hasMoreTokens()) {
            String token = tokanizer.nextToken();

            int split = token.indexOf('=');
            if (split >= 0) {
                nv.put(URLDecoder.decode(token.substring(0, split), "UTF-8").trim(),
                        URLDecoder.decode(token.substring(split + 1), "UTF-8"));
            } else {
                nv.put(URLDecoder.decode(token, "UTF-8").trim(), "");
            }
        }
    }

	
}
