package dadad.system.api.impl;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import dadad.data.config.APIConfiguration;
import dadad.platform.AnnotatedException;
import dadad.platform.Context;
import dadad.platform.PlatformDataType;
import dadad.system.SystemInterface;
import dadad.system.WorkKernel;
import dadad.system.api.API;
import dadad.system.api.APIDispatcher;
import dadad.system.api.APIImpl;
import dadad.system.api.http.HttpRedirectException;
import dadad.system.data.APIScriptWorkflow;

import static dadad.platform.Constants.NEWLINE;

/**
 * Server api implementation.
 */
public class ServerAPI_API extends APIImpl {

	// ===============================================================================
	// = FIELDS
	
	public final static long PERSISTENCE_LIMIT_MS = 60 * 60 * 2 * 1000;  // Two hours
	
	
	// ===============================================================================
	// = ABSTRACT
	
	public long persistanceLimit() {
		return PERSISTENCE_LIMIT_MS;
	}
	
	// ===============================================================================
	// = METHOD
	
	public String script(final PlatformDataType dataType, final String file) {
		File contextFile = new File(file);
		
		String script = "";
		try {
			if (! contextFile.canRead()) throw new AnnotatedException("Cannot run script because file cannot be read.");				
			
			byte[] encoded;
			try {
				encoded = Files.readAllBytes(Paths.get(file));
				script = new String(encoded, "UTF-8");
			} catch (IOException e) {
				throw new AnnotatedException("Failed to read script file.", e);
			}
			
		} catch (AnnotatedException ae) {
			throw ae.annotate("file.path", file);
		}
		
		return scriptprop(dataType, script);
	}

	public String scriptprop(final PlatformDataType dataType, final String script) {
		SystemInterface si = WorkKernel.getSystemInterface();
		Context newContext = si.getNewContext(); 
		newContext.getConfig().set(APIConfiguration.API_SCRIPT, script);
		return WorkKernel.getSystemInterface().startWorkProcess(APIScriptWorkflow.class.getName(), newContext);
	}
	
	public Object help(final PlatformDataType dataType, final String apiName) {
		StringBuilder result = new StringBuilder();

		APIDispatcher dispather = WorkKernel.getSystemInterface().getAPIDispatcher();
		API[] apis = dispather.getAPI(apiName);
		
		try {
			if (apis == null) throw new AnnotatedException("API does not exist");
		
			switch (dataType) {
			
			case DTMP:
			case TEXT:
				result.append("====================================================================================================").append(NEWLINE);
				helpAPIText(apis, result);
				result.append(NEWLINE);
				break;
				
			case JSON:				
			case DATA:
				return apis;
				
			case HTML:
			case SNIP:
				throw new HttpRedirectException("apihelp.dtemp?api.name=" + apiName);
			}
			
		} catch (AnnotatedException ae) {
			throw ae.annotate("api.name", apiName);
		}
	
		return result.toString();
	}
	
	public Object helpall(final PlatformDataType dataType) {
		
		StringBuilder result = new StringBuilder();
		
		APIDispatcher dispather = WorkKernel.getSystemInterface().getAPIDispatcher();
		Collection<Class<?>> apisClasses = dispather.getAPIs();
				
		switch (dataType) {

		case TEXT:
		case DTMP:
			for (Class<?> apiClass : apisClasses) {
				API[] apis = APIDispatcher.getAPI(apiClass);
				result.append("==========================================================================================").append(NEWLINE);	
				helpAPIText(apis, result);
				result.append(NEWLINE);			
			}
			break;
			
		case JSON:			
		case DATA:
			// Not sure this is helpful at all.
			return apisClasses;
			
		case HTML:
		case SNIP:
			throw new HttpRedirectException("apilist.dtemp");
		}
	
		return result.toString();		
	}
	
	private void helpAPIText(final API[] apis, final StringBuilder result) {
		result.append("name: API").append(NEWLINE);
		result.append(apis[0].getAPIHelp()).append(NEWLINE);
		
		for (API api : apis) {
			result.append("--------------------------------------------------------------------------------").append(NEWLINE);	
			result.append("-- method: ").append(api.getMethodName()).append(NEWLINE);
			result.append(api.getAPIHelp()).append(NEWLINE);
			result.append("-- return type: ").append(api.getResultClass().getName()).append(NEWLINE);
			result.append(api.getResultHelp()).append(NEWLINE);
			
			String[] paramNames = api.getParameterNames();
			String[] paramHelp = api.getParameterHelp();
			for (int index = 0; index < paramNames.length; index++) {
				result.append("-- parameter #").append(index).append(" : ").append(paramNames[index]).append(NEWLINE);						
				result.append(paramHelp[index]).append(NEWLINE);
			}
			
			result.append(NEWLINE);
		}
	}

}
