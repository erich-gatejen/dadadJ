package dadad.system.api;

/**
 * API interface.
 */
public interface API {
	
	/**
	 * Used for serving none API requests.
	 */
	public final static String RESERVED_API_NAME = "dadad";

    public String getAPIName();
    
    public String getAPIHelp();

    public String getMethodName();

    public String getMethodHelp();

    public Class<?> getResultClass();

    public String getResultHelp();
    
    public Class<?> getDispatchClass(); 
    
    public String getDispatchMethod(); 
    
    public String[] getParameterNames();
    
    public Object[] getParameterValidations();

    public String[] getParameterHelp();

}

