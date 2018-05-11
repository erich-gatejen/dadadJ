package dadad.platform.services;

public enum LoggerLevel {

	DATA("DATA "),
	FAULT("FAULT"),	
	ERROR("ERROR"),
	WARN("WARN "),
	INFO("INFO "),
	DEBUG("DEBUG"),
	TRACE("TRACE");
	
	final String text;
	
	private LoggerLevel(String text) {
	    this.text = text;
	}
	
	public String text() {
	    return text;
	}
	
	public boolean isEnabled(final LoggerLevel level) {
	    if (this.ordinal() >= level.ordinal())
	        return true;
	    return false;
	}
	
}
