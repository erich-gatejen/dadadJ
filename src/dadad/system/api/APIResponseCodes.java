package dadad.system.api;

/**
 * Response codes.  They are loosely based on HTTP.
 */
public enum APIResponseCodes {

    SUCCESS(200, true, "API call successful."),
    ERROR(400, false, "API call created an error."),
    NOT_FOUND(404, false, "API call not found."),
    FAULT(500, false, "API call caused a system fault."),
    BAD_CODE(999, false, "This is an invalid code.");

    private final int code;
    private final boolean success;
    private final String text;
    private APIResponseCodes(final int code, final boolean success, final String text) {
        this.code = code;
        this.success = success;
        this.text = text;
    }

    public int getCode() { 
    	return code; 
    }

    public boolean isSuccessful() {
    	return success; 
    }
    
    public boolean isFailure() {
    	return ! success;
    }
    
    public String text() {
    	return text;
    }

    public static APIResponseCodes getCode(int code) {
        switch(code) {
            case 200 : return APIResponseCodes.SUCCESS;
            case 201 : return APIResponseCodes.SUCCESS;
            case 400 : return APIResponseCodes.ERROR;
            case 404 : return APIResponseCodes.NOT_FOUND;
            case 500 : return APIResponseCodes.FAULT;
            default  : return APIResponseCodes.BAD_CODE;
        }
    }
}

