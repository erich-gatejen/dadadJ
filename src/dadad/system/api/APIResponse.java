package dadad.system.api;

import dadad.platform.PlatformDataType;

/**
 * API response class.
 */
public class APIResponse {

    public APIResponseCodes code;
    public PlatformDataType dataType;

    /**
     * For the data type DATA, this will be a java object.  Otherwise it will be a String.
     */
    public Object response;

    /**
     * If the call resulted in a server exception AND the data type is DATA, then this 
     * should be the exception from the server.  For all the other data types, the exception
     * would have come from the client.  The presence of an exception can happy with ANY
     * code.
     */
    public Throwable exception;
    
    public APIResponse() {
    	
    }
    
    public APIResponse(final APIResponseCodes code, final PlatformDataType dataType, final Object response) {
    	this.code = code;
    	this.dataType = dataType;
    	this.response = response;
    }
    
    public APIResponse(final APIResponseCodes code, final PlatformDataType dataType, final Object response, final Throwable exception) {
    	this(code, dataType, response);
    	this.exception = exception;
    }
    
}

