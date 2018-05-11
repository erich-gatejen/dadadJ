package dadad.system.api.http;

public class HttpRedirectException extends RuntimeException {
	
	
	// ===============================================================================
	// = FIELDS

	private static final long serialVersionUID = 1L;

	public final String url;
	
	// ===============================================================================
	// = METHODS
	
	public HttpRedirectException(final String url) {
		super("BUG BUG BUG!  This should never be seen.  This exception must be caught.");
		this.url = url;
	}
	


}
