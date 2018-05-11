package dadad.platform;

/**
 * Data type.
 */
public enum PlatformDataType {

	TEXT("text/plain"), 
	DATA("application/x-java-object"), 
	JSON("application/json"), 
	HTML("text/html"),
	
	// Template intermediary
	DTMP("text/html"),
	
	// HTML snippet
	SNIP("text/html");

	private final String contentType;
	private PlatformDataType(final String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}
}
