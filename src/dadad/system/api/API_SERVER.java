package dadad.system.api;

import dadad.platform.config.validation.ValidationNOP;
import dadad.platform.config.validation.ValidationNotEmpty;
import dadad.system.WorkProcessState;
import dadad.system.api.impl.ServerAPI_SERVER;

/**
 * Basic system interactions.
 */
public enum API_SERVER implements API {

	PING("Ping the server.", String.class, "It echos the sent string", ServerAPI_SERVER.class, 
			"ping",
			new String[]{ "text" }, 
			new Object[]{ new ValidationNotEmpty() }, 
			new String[]{ "The ping text." }),
	
	START("Start a work process.", String.class, "The name of the new work process", ServerAPI_SERVER.class,  
			"start",
			new String[]{ "classname" }, 
			new Object[]{ new ValidationNotEmpty() }, 
			new String[]{ "The work process implementation.  It might implement the WorkProcessContainer interface." }),
	
	SET("Set a property.", String.class, "It echos propery name.", ServerAPI_SERVER.class, 
			"set",
			new String[]{ "name", "value" }, 
			new Object[]{ new ValidationNotEmpty(), new ValidationNOP() }, 
			new String[]{ "Name of the property.", "Value of the property (may be encoded as a multivalue)." }),

	GET("Get a property.", String.class, "The property value (may be encoded as a multivalue).", ServerAPI_SERVER.class, 
			"get",
			new String[]{ "name" }, 
			new Object[]{ new ValidationNotEmpty() },
			new String[]{ "Name of the property." }),
	
	LOAD("Load properties from a file.", String.class, "Echos the file name.", ServerAPI_SERVER.class, 
			"load",
			new String[]{ "path" }, 
			new Object[]{ new ValidationNotEmpty() },
			new String[]{ "Path relative to the install root." }),
	
	STATE("Get work process state.", WorkProcessState.class, "The current state of the work process.", ServerAPI_SERVER.class, "status",
			new String[]{ "name" }, 
			new Object[]{ new ValidationNotEmpty() },
			new String[]{ "Name of the process as returned from the START call." }),
	
	PROPERTIES("Get the current root properties.", Object.class, "The current properties.  If datatype is TEXT, it will be a loadable properties text.  "
			+ "If datatype is DATA, it will be a Java Map, which will also be convered for JSON.  HTML is man-readable.", ServerAPI_SERVER.class, 
			"properties",
			new String[]{  }, 
			new Object[]{  },
			new String[]{  }),
	
	WPPROPERTIES("Get the properties for a work process.", Object.class, "The current properties.  If datatype is TEXT, it will be a loadable properties text.  "
			+ "If datatype is DATA, it will be a Java Map, which will also be convered for JSON.  HTML is man-readable.", ServerAPI_SERVER.class, 
			"wpproperties",
			new String[]{ "name" }, 
			new Object[]{ new ValidationNotEmpty() },
			new String[]{ "Name of the process as returned from the START call." }),
	
	PROCLIST("Get the process list.", Object.class, "The current process list.  If datatype is TEXT, it will be a loadable properties text.  "
			+ "If datatype is DATA, it will be a Java Map of names mapped to WorkProcessState, which will also be convered for JSON.  HTML is man-readable.", ServerAPI_SERVER.class, 
			"proclist", 
			new String[]{  }, 
			new Object[]{  },
			new String[]{  }),
	
	STOP("Stop the server.", String.class, "It echos the sent string", ServerAPI_SERVER.class, 
			"stop",
			new String[]{ "text" }, 
			new Object[]{ new ValidationNotEmpty() }, 
			new String[]{ "The ping text." }),
	
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
	private API_SERVER(final String help, final Class<?> resultClass, final String resultHelp, final Class<?> dispatchClass,
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
	public String getAPIName() { return "SERVER"; }
	public String getAPIHelp() { return "Server control."; }
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

