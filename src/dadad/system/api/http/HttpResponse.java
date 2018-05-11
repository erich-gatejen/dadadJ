package dadad.system.api.http;

import java.io.InputStream;

import dadad.system.api.APIResponseCodes;

/**
 * HTTP response class.
 */
public class HttpResponse {

    public APIResponseCodes code;
    public String mimeType;
    public long length;

    /**
     * Response stream.  You must close!
     */
    public InputStream response;

    
    public HttpResponse() {
    	
    }
    
    public HttpResponse(final APIResponseCodes code, final String mimeType, final InputStream response, final long length) {
    	this.code = code;
    	this.mimeType = mimeType;
    	this.response = response;
    	this.length = length;
    }

    
}

