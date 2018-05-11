package dadad.platform;

public class PuntException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public PuntException() {
		super();
	}
	
	public PuntException(final String message) {
		super(message);
	}
	
	public PuntException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
