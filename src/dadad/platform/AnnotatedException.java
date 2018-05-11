package dadad.platform;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import static dadad.platform.Constants.NEWLINE;

public class AnnotatedException extends RuntimeException {

	final static long serialVersionUID = 1;
	
	public enum Catagory {
		NORMAL,
		ERROR,
		FAULT,
		PANIC;
	}

	// ===============================================================================
	// = FIELDS

	/**
	 * Annotations
	 */
	protected HashMap<String, String> annotations = new HashMap<String, String>();
	
	private final Catagory catagory;

	// ===============================================================================
	// = METHODS
	
	public AnnotatedException(final String message) {
		super(message);
		this.catagory = Catagory.NORMAL;
	}
	
	public AnnotatedException(final String message, Throwable cause) {
		super(message, cause);
		this.catagory = Catagory.NORMAL;
	}
	
	public AnnotatedException(final String message, final Catagory catagory) {
		super(message);
		this.catagory = catagory;
	}
	
	public AnnotatedException(final String message, final Catagory catagory, Throwable cause) {
		super(message, cause);
		this.catagory = catagory;
	}

	public Catagory catagory() {
		return catagory;
	}
	
	public boolean worseThan(final Catagory catagory) {
		if (this.catagory.ordinal() > catagory.ordinal()) return true;
		return false;
	}
	
	public HashMap<String, String> annotations() {
		return annotations;
	}
	
	public AnnotatedException annotate(final String name, final Object value) {
		annotations.put(name, value.toString());		
		return this;
	}

	public AnnotatedException annotate(Object... nv) {
		if (nv == null) throw new Error("BUG: cannot annotate null nv.");
		if ((nv.length % 2) > 0) throw new Error("BUG: nv must be even name/value pairs.");
		for (int rover = 0; rover < nv.length; rover = rover + 2) {
			annotate(nv[rover].toString(), nv[rover + 1].toString());
		}
		return this;
	}

	public static String render(Throwable t) {
		return render(t, false);
	}
	
	public static String render(Throwable t, final boolean traces) {
		StringWriter sw = new StringWriter();
		try {

			Throwable current = t;
			Throwable candidate;
			while (current != null) {
				sw.append("====").append(current.getClass().getName()).append(" ------------------").append(NEWLINE);				
				sw.append("message: ").append(current.getMessage()).append(NEWLINE);
				
				// Annotated
				if (current instanceof AnnotatedException) {
					sw.append("catagory: ").append(((AnnotatedException) current).catagory.name()).append(NEWLINE);
					int size = ((AnnotatedException) current).annotations.size(); 
					if (size > 0) {
						sw.append("attributes: ");
						for (String name : ((AnnotatedException) current).annotations.keySet()) {
							sw.append("name=[").append(name).append("] value=[")
								.append(((AnnotatedException) current).annotations.get(name)).append("]").append(NEWLINE);
							size--;
							if (size > 0) sw.append("          : "); 
						}

					}
					
				} else if (current instanceof ExceptionBundleException) {
					sw.append( ((ExceptionBundleException) current).render(traces) );
					
				}

				candidate = current.getCause();
				if (candidate == current)
					candidate = null; 		// For some reason they point to themselves sometimes.
				current = candidate;
			}

			// Stacktrace
			sw.append("stacktrace: ").append(NEWLINE);
			PrintWriter stp = new PrintWriter(sw);
			t.printStackTrace(stp);
			stp.flush();
			sw.append(NEWLINE);

			// Footer
			sw.flush();

		} catch (Exception ee) {
			throw new Error("BUG: Spurious error trying to render exception.  ", ee);
		}
		return sw.toString();
	}

}
