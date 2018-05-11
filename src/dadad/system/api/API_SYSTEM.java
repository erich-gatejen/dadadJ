package dadad.system.api;

import dadad.platform.config.validation.ValidationNotEmpty;
import dadad.system.api.impl.ServerAPI_SYSTEM;

/**
 * Basic system interactions.
 */
public enum API_SYSTEM implements API {

	LOAD("Load a file into a property.  No encoding or decoding is done.", String.class, "The contents of the file in a string.", ServerAPI_SYSTEM.class, 
			"load",
			new String[]{ "file.path" }, 
			new Object[]{ new ValidationNotEmpty() }, 
			new String[]{ "The contents of the file in a string." }),
	
	SAVE("Save text into a file.  No encoding or decoding is done.", String.class, "The text that is saved", ServerAPI_SYSTEM.class, 
			"save",
			new String[]{ "file.path", "text" }, 
			new Object[]{ new ValidationNotEmpty(), new ValidationNotEmpty() }, 
			new String[]{ "Path to the file.", "The text to save." }),
	
	REPORTLOG("Get url to a report log.", String.class, "The path to the url to the log file.", ServerAPI_SYSTEM.class, 
			"reportlog",
			new String[]{ "work.process.name" }, 
			new Object[]{ new ValidationNotEmpty() }, 
			new String[]{ "Name of the work process for the report file." })
	
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
	private API_SYSTEM(final String help, final Class<?> resultClass, final String resultHelp, final Class<?> dispatchClass,
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
	public String getAPIName() { return "SYSTEM"; }
	public String getAPIHelp() { return "System interaction methods."; }
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

