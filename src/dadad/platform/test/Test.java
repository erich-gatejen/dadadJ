package dadad.platform.test;

import dadad.data.model.Result;
import dadad.data.model.ResultType;
import dadad.platform.Context;
import dadad.platform.ContextRunnable;

/**
 * A test.  Not like a QA test, but a test test to see if the teted thing does what is tested.  That kind of test.
 * <br>
 * A call to run (interface ContextRunnable) will run the test.  In terms of the RESULT, 
 * each call to this must be completely stateless.  Any test can be executed any arbitrary number 
 * of times with whatever context configurations.   The system, context or whatever may keep as much state as it wants.
 * Any return will be considered a pass.  Any exception will be a fault.   Calls to 
 * helper methods below will act accordingly.
 * 
 */
public abstract class Test implements TestRunnable {
	
	// ===============================================================================
	// = ABSTRACT
	
	/**
	 * Run the test.
	 * @param context test context.
	 * @param target test target.
	 */
	abstract public void _run(final Context context, final Object target);
	
		
	// ===============================================================================
	// = FIELDS
	
	public final static char NAMESPACE_SEPARATOR = '.';

	private Result currentResult;
	
	// ===============================================================================
	// = METHODS

	/**
	 * Construct the test. 
	 */
	public Test() {
		super();
		currentResult = Result.newResult(name());
	}

	public void fail(final String reason) {
		throw new TestFailException(reason);
	}

	public void fail(final String reason, final Throwable t) {
		throw new TestFailException(reason).add(t);
	}

	public void fault(final String reason, final Throwable t) {
		throw new TestFailException(reason).add(t).isFault();
	}


	// ===============================================================================
	// = INTERFACE

	/**
	 * Run the test.  In most cases, you would never call this directly.
	 * @param namespace current namespace for the test.  Allows organization.
	 * @param context current context.
	 * @param target test target
	 * @return the final Result
	 */
	public Result run(final String namespace, final Context context,  final Object target) {
		
		currentResult = Result.start(name());
		
		String fqname = name();
		if (namespace != null) fqname = namespace + NAMESPACE_SEPARATOR + fqname;	
		
		try {
			_run(context, target);
			currentResult.transition(ResultType.PASSED);

		} catch (TestFailException tfe) {
			currentResult.transition(ResultType.FAILED)
				.message(tfe.getMessage());
			if (tfe.cause != null) currentResult.fault(tfe.cause);
			
		} catch (Exception e) {
			// DO NOT LET EXCEPTIONS OUT!!!
			currentResult.transition(ResultType.FAULT)
				.message(e.getMessage())
				.fault(e);
		}	
		currentResult.close();

		return currentResult;
	}

	// ===============================================================================
	// = INTERNAL
		

	
}
