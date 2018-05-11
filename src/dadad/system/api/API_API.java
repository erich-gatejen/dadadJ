package dadad.system.api;

import dadad.platform.config.validation.ValidationNotEmpty;
import dadad.system.api.impl.ServerAPI_API;

/**
 * Working with APIs.
 */
public enum API_API implements API {

	SCRIPT("Run an API script.", String.class, "Work process name for the script.", ServerAPI_API.class, 
			"script",
			new String[]{ "script.file" }, 
			new Object[]{ new ValidationNotEmpty() }, 
			new String[]{ "Path to the script file." }),
	
	SCRIPTPROP("Run an API script.", String.class, "Work process name for the script.", ServerAPI_API.class, 
			"scriptprop",
			new String[]{ "script.text" }, 
			new Object[]{ new ValidationNotEmpty() }, 
			new String[]{ "Text of the script." }),
	
	HELP("Get help for a specific API.", API.class, "The help.", ServerAPI_API.class, 
			"help",
			new String[]{ "api.name" }, 
			new Object[]{ new ValidationNotEmpty() }, 
			new String[]{ "The API name as it appears under server.api. in the configured API registry." }),
	
	HELPALL("Get all the API that can give help.", String.class, "The list of APIs.  If HTML, it will contain links to specific help, otherwise it is just a text list.", ServerAPI_API.class, 
			"helpall",
			new String[]{  }, 
			new Object[]{  }, 
			new String[]{  } )
	
	;

	// == BOILERPLATE - don't touch =================================================================	
	private final String	help;
	private final Class<?>	resultClass;
	private final String	resultHelp;	
	private final Class<?>  dispatchClass;
	private final String 	dispatchMethod;
	private final String[]	parameterNames;
	private final Object[]  parameterValidations;
	private final String[]	parameterHelp;
	private API_API(final String help, final Class<?> resultClass, final String resultHelp, final Class<?> dispatchClass,
			final String dispatchMethod, final String[] parameterNames, final Object[] parameterValidations, final String[] parameterHelp) {
	    this.help = help;
	    this.resultClass = resultClass;
	    this.resultHelp = resultHelp;
	    this.dispatchClass = dispatchClass;
	    this.dispatchMethod = dispatchMethod;
	    this.parameterNames = parameterNames;
	    this.parameterValidations = parameterValidations;
	    this.parameterHelp = parameterHelp;	   
	}
	public String getAPIName() { return "API"; }
	public String getAPIHelp() { return "API access methods."; }
	public String getMethodName() { return this.name(); }
	public String getMethodHelp() { return help; }
	public Class<?> getResultClass() { return resultClass; }
	public String getResultHelp() { return resultHelp; }
	public Class<?> getDispatchClass() { return (Class<?>) dispatchClass; }
	public String getDispatchMethod() { return dispatchMethod; } 
	public String[] getParameterNames() { return parameterNames; }
	public Object[] getParameterValidations() { return parameterValidations; }
	public String[] getParameterHelp() { return parameterHelp; }
	// ===============================================================================================

}

