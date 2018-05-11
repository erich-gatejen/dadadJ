package dadad.platform;

import java.io.StringWriter;
import java.util.LinkedList;

import static dadad.platform.Constants.NEWLINE;

public class ExceptionBundleException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	// ===============================================================================
	// = FIELDS

	private LinkedList<Exception> exceptionBundle = new LinkedList<Exception>();

	// ===============================================================================
	// = METHODS
	
	public ExceptionBundleException(final String message) {
		super(message);
	}
	
	public ExceptionBundleException(final String message, Throwable cause) {
		super(message, cause);
	}
	
	public void addException(Exception e) {
		exceptionBundle.add(e);
	}
	public LinkedList<Exception> getExceptionBundle() {
		return exceptionBundle;
	}

	public String render() {
		return render(false);
	}
	
	public String render(final boolean traces) {
		StringWriter sw = new StringWriter();
		try {
			
			sw.append("=: Exception Bundle =:");
			int number = 1;
			
			for (Exception e : exceptionBundle) {
				sw.append("=== #").append(Integer.toString(number))
					.append(" ==========================================================================").append(NEWLINE);				
				sw.append(AnnotatedException.render(e));
				number++;
			}

			sw.append(NEWLINE);
			sw.flush();

		} catch (Exception ee) {
			throw new Error("BUG: Spurious error trying to render exception bundle.", ee);
		}
		return sw.toString();
	}

}
