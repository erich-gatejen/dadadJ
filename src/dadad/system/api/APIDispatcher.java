package dadad.system.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import dadad.platform.AnnotatedException;
import dadad.platform.Context;
import dadad.platform.PlatformDataType;
import dadad.platform.config.validation.Validation;
import dadad.system.SystemConfiguration;

/**
 * API Dispatch.
 */
public class APIDispatcher {
	
	// ===============================================================================
	// = FIELDS
	
	private final Map<String, Class<?>> apiMap = new HashMap<String, Class<?>>();
	private final Map<String, API> apiEnumMap = new HashMap<String, API>();
	
	private final Map<String, APIImpl> implMap = new HashMap<String, APIImpl>();
	private final Map<String, Method> methodMap = new HashMap<String, Method>();
	private final Map<String, HashMap<String, Integer>> parameterMappingMap = new HashMap<String, HashMap<String, Integer>>();
	
	private static Pattern pathSplitter = Pattern.compile("\\/");	
	public final static int NAME_PART = 0;
	public final static int COMMAND_PART = 1;
	public final static int DATATYPE_PART = 2;
	
	// ===============================================================================
	// = METHODS

	public void configure(final Context context) {
		
		int prefixSize = SystemConfiguration.SERVER_API_LIST.property().length() + 1;   // +1 for the . separator.
		Map<String, String[]> apiList = context.getConfig().getPly(SystemConfiguration.SERVER_API_LIST);
		for (String name : apiList.keySet()) {
			
			if ((name.length() > prefixSize)) {
				String[] apiClass = apiList.get(name);
			
				if ((apiClass.length > 0) && (apiClass[0].trim().length() > 0)) {
				
					try {
						String apiName  = name.substring(prefixSize).toUpperCase();
						String apiClassName = apiClass[0].trim();
						
						if (apiName.equals(API.RESERVED_API_NAME)) {
							throw new AnnotatedException("Tbis is a reserved API name.  You may not use it.")
								.annotate("property.name", name, "api.name", apiName);						
						}
								
						if (apiMap.containsKey(apiName)) {
							throw new AnnotatedException("API of the name already defined")
								.annotate("property.name", name, "api.name", apiName);
						}
						apiMap.put(apiName.toUpperCase(), Class.forName(apiClassName));
				
					} catch (AnnotatedException ae) {
						throw ae;
					} catch (Exception e) {
						throw new AnnotatedException("Unable to load class for listed API.", e)
							.annotate("property.name", name, "value", apiList.get(name)[0]);
					}
				
				}			
			}
			
		} // end for
		
	}
	
	public Class<?> findAPIClass(final String apiName) {
		return apiMap.get(apiName.trim().toUpperCase());
	}

	public Set<String> getAPINames() {
		return apiMap.keySet();
	}
	
	public APIRequest parse(final String path) {
		APIRequest request = null;
		
		String normalPath;
		if ((path.length() > 0) && (path.charAt(0) == '/')) normalPath = path.substring(1);
		else normalPath = path;
		
	    String[] pathElements = pathSplitter.split(normalPath);
	    if (pathElements.length == 3) {

	    	Class<?> clazz = findAPIClass(pathElements[NAME_PART]);
	    	if (clazz != null) {
	    		request = new APIRequest();
	    		request.name = pathElements[NAME_PART];
	    		request.apiClass = clazz;
	    		request.command = pathElements[COMMAND_PART];
	    		
		    	try {
		    		request.dataType = PlatformDataType.valueOf(pathElements[2].toUpperCase());	    		
		    	} catch (Exception e) {
		    		 throw new AnnotatedException("Invalid data type").annotate("data.type", pathElements[DATATYPE_PART]);  		
		    	}
	    	}
	    	    	
	    }	
		
		return request;
	}
		
	public APIResponse dispatcher(final APIRequest request, final Map<String, String> parameters) {
		APIResponse result;
		
		try {
    		result = dispatch(request.name, request.apiClass, request.command, request.dataType, parameters);
    	} catch (Exception e) {
    		result = new APIResponse(APIResponseCodes.FAULT, request.dataType, "API call failed", e);   
    	}

		return result;
	}
	
    @SuppressWarnings("unchecked")
	public APIResponse dispatch(final String apiName, final Class<?> apiClass, final String command, final PlatformDataType dataType, final Map<String, String> nv) {
    	APIResponse result = null;
    	
    	if (apiClass == null) {
    		return new APIResponse(APIResponseCodes.FAULT, dataType, "API does not exist.", 
    				new AnnotatedException("API not registered in " + SystemConfiguration.SERVER_API_LIST.property() + ".")
    					.annotate("api.name", apiName));	
    	}
    	
    	API api;
    	String expectedCommand = command.trim().toUpperCase();
    	String commandMapName = apiName + '*' + expectedCommand;
    	api = apiEnumMap.get(commandMapName);
    	if (api == null) {
	    	try {   		
		        api = (API) findEnumValue((Class<? extends Enum<?>>)  apiClass, expectedCommand);		        
	    	} catch (ClassCastException cce) {
	    		return new APIResponse(APIResponseCodes.FAULT, dataType, "Proposed API class does not implement API interface.", 
	    				new AnnotatedException(cce.getMessage(), cce).annotate("api.name", apiName, "expected.class", apiClass.getName()));		    		        
	    	} catch (Exception e) {
	    		return new APIResponse(APIResponseCodes.FAULT, dataType, "API could not be mapped due to spurious exception.", 
	    				new AnnotatedException(e.getMessage(), e).annotate("api.name", apiName, "expected.class", apiClass.getName()));	
	    	}  
    	}
    	if (api == null) return new APIResponse(APIResponseCodes.FAULT, dataType, "API does not exist.",
    			new AnnotatedException("API does not exist.").annotate("api.name", apiName, "api.command", expectedCommand));

    	String className = api.getDispatchClass().getName();
    	Object target = implMap.get(className);
    	try {
	    	if (target == null) {

	    		if (APIImpl.class.isAssignableFrom(api.getDispatchClass())) {
	    			APIImpl impl = (APIImpl) api.getDispatchClass().newInstance();
	    			implMap.put(className, impl);
	    			target = impl;
	    			
	    		} else {
	    			target = api.getDispatchClass().newInstance();
	    			
	    		}    		
	    	}
	    	
    	} catch (Exception e) {
        	return new APIResponse(APIResponseCodes.FAULT, dataType, "Unable to get instance of API implementation.",
        			new AnnotatedException("Unable to get instance of API implementation.").annotate("api.name", apiName, "api.command", expectedCommand));   		
    	}
    	
    	Class<?> actualClass = target.getClass();
    	String methodMappingName = commandMapName + '*' + api.getDispatchMethod(); 
    	Method method = methodMap.get(methodMappingName);
    	if (method == null) {
    		String dispatchMethodName = api.getDispatchMethod().toLowerCase();    		
    		Method[] methods = actualClass.getDeclaredMethods();
    		for (Method candidateMethod : methods) {
    			if (candidateMethod.getName().toLowerCase().equals(dispatchMethodName)) {
    				method = candidateMethod;
    				methodMap.put(methodMappingName, method);
    				break;
    			}
    		}    		
    	}
    	if (method == null) return new APIResponse(APIResponseCodes.FAULT, dataType, "Method does not exist in API.",
    			new AnnotatedException("Method in API does not exist.").annotate("api.name", apiName, "api.command", expectedCommand,
    					"method", api.getDispatchMethod()));
    	
    	HashMap<String, Integer> parameterMap = parameterMappingMap.get(methodMappingName);
    	if (parameterMap == null) {
    		parameterMap = new HashMap<String, Integer>();
    		int index = 1;
    		for (String parameter : api.getParameterNames()) {
    			String normalized = parameter.toLowerCase();
    			if (parameterMap.containsKey(normalized)) throw new Error("BUG BUG BUG!  Method has two paramters by the same name.  name=" + normalized);
    			parameterMap.put(normalized, index);
    			index++;
    		}
    		parameterMappingMap.put(methodMappingName, parameterMap);
    	}
    	
    	Object[] parameters = new Object[api.getParameterNames().length + 1];
    	parameters[0] = dataType;
    	for (String passed : nv.keySet()) {
    		String normalName = passed.toLowerCase();
    		Integer paramSpot = parameterMap.get(normalName);
    		if (paramSpot != null) {
    			parameters[paramSpot] = nv.get(passed);
    		}
    	}
    	
    	// Validate parameters
    	Object[] validations = api.getParameterValidations();
    	if (validations.length < (parameters.length - 1)) throw new Error("BUG BUG BUG!  The param names don't match validations for API.");
		for (int vindex = 0; vindex < validations.length; vindex++) {
			
			Object validation = validations[vindex];
			
			if (validation instanceof Validation) {
			
				((Validation) validations[vindex]).validate(parameters[vindex + 1], "Validating parameter [" + api.getParameterNames()[vindex] + "]", true);
				
			} else if (validation instanceof Class<?>) {
				
				Class<?> validationClass = (Class<?>) validation ;
				
				if (validationClass.isEnum()) {
					
					Enum<?> value = findEnumValue((Class<? extends Enum<?>>) validationClass, validation.toString());
					if (value == null) {
						throw new AnnotatedException("Parameter " + parameters[vindex + 1] + " not a valid enum value for enum " + validationClass.getName());
					}
				
				}
			}
		}   
		
    	try {
    		Object invokeResult = method.invoke(target, parameters);
    		if (invokeResult == null) {
    			if (method.getReturnType().isAssignableFrom(String.class)) invokeResult = "";
    			else invokeResult = new Object();
    		}
    		result = new APIResponse(APIResponseCodes.SUCCESS, dataType, invokeResult);
    	
    	} catch (ClassCastException cce) {
    		return new APIResponse(APIResponseCodes.FAULT, dataType, "API method did not return a APIResponse.",
        			new AnnotatedException("API method did not return a APIResponse.").annotate("api.name", apiName, "api.command", expectedCommand,
        					"method", api.getDispatchMethod()));
    		
    	} catch (IllegalAccessException iae) {
    		return new APIResponse(APIResponseCodes.FAULT, dataType, "API method call caused an IllegalAccessException.",
        			new AnnotatedException("API method call caused an IllegalAccessException.", iae).annotate("api.name", apiName, "api.command", expectedCommand,
        					"method", api.getDispatchMethod()));

    	} catch (IllegalArgumentException iarge) {
    		return new APIResponse(APIResponseCodes.FAULT, dataType, "API method called with wrong arguments.",
        			new AnnotatedException("API method called with wrong arguments.", iarge).annotate("api.name", apiName, "api.command", expectedCommand,
        					"method", api.getDispatchMethod()));
    		
    	} catch (InvocationTargetException ite) {
    		Throwable cause = ite.getTargetException();
    		AnnotatedException newException;
    		if (cause instanceof AnnotatedException) {
    			newException = (AnnotatedException) cause; 
    		} else {
    			newException = new AnnotatedException("API call failed due to exception.", cause);
    		}
    		newException.annotate("api.name", apiName, "api.command", expectedCommand, "method", api.getDispatchMethod());
    		result = new APIResponse(APIResponseCodes.FAULT, dataType, "API call failed due to exception.", newException);
    	}
    	
    	return result;
    }
       
	public static Enum<?> findEnumValue(Class<? extends Enum<?>> enumType, String value) {
	    return Arrays.stream(enumType.getEnumConstants())
	                 .filter(e -> e.name().equals(value))
	                 .findFirst()
	                 .orElse(null);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Enum[] getEnumValues(Class<?> enumType) {
		Class<? extends Enum<?>> casted = null;
		try {
			casted = (Class<? extends Enum<?>>) enumType;
		} catch (Exception e) {
			throw new Error("BUG BUG BUG!  A class that isn't an enum passed to getEnumValues.  class=" + enumType.getName());
		}		
	    return casted.getEnumConstants();
	}	
	
	public Collection<Class<?>> getAPIs() {
		return apiMap.values();
	}
	
	@SuppressWarnings("unchecked")
	public API[] getAPI(final String name) {
		Class<?> apiClass = apiMap.get(name);
		if (apiClass == null) return null;
		return (API[]) getEnumValues((Class<? extends Enum<?>>) apiClass);
	}
	
	@SuppressWarnings("unchecked")
	public static API[] getAPI(final Class<?> apiClass) {
		return (API[]) getEnumValues((Class<? extends Enum<?>>) apiClass);
	}
    
	
}
