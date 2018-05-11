package dadad.platform.test;

/**
 * TestFailException.
 * 
 */
public class TestFailException extends RuntimeException {
		
	// ===============================================================================
	// = FIELDS
	
	private static final long serialVersionUID = 1L;
	
	Throwable cause;
	boolean isFault;
	
	// ===============================================================================
	// = METHODS

	public TestFailException(final String message) {
		super(message);
	}
	
	public TestFailException add(final Throwable cause) {
		this.cause = cause;
		return this;
	}
	
	public TestFailException isFault() {
		isFault = true;
		return this;
	}

}
