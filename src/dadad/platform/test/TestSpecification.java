package dadad.platform.test;

import dadad.data.model.Result;
import dadad.data.model.ResultType;
import dadad.platform.Context;
import dadad.platform.ContextRunnable;
import dadad.platform.services.Logger;
import dadad.platform.services.PlatformInterfaceRequestor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A collection of test cases.
 * <br>
 */
public abstract class TestSpecification implements TestRunnable {

	// ===============================================================================
	// = ABSTRACT

	/**
	 * Setup.  It will be called before any test cases are called.
	 */
	abstract public void _setup();

    /**
     * Teardown.  It will be called after all test cases are called (or aborted).
     */
    abstract public void _teardown();

    /**
     *  Is the test specification terminal.  If it is terminal, it will not propagate faults from here, though the
     *  result will obviously not be a PASS.
     *  @return true if it is terminal, otherwise false.
     */
    abstract public boolean _isTerminal();


	// ===============================================================================
	// = FIELDS

	class Sub implements Comparable<Sub> {
        final Method method;
		final TestCase.Type type;
		final int  priority;
		final String name;
		public Sub(final Method method) {
		    this.method = method;
            Annotation annotation = method.getAnnotation(TestCase.class);
            TestCase testCase = (TestCase) annotation;
 		    this.type = testCase.type();
 		    this.priority = testCase.priority();
			this.name = method.getName();
		}

        public int compareTo(Sub o) {
            return priority - o.priority;
        }
	}

	class EndTest extends RuntimeException {
        ResultType resultType;
	    public EndTest(final ResultType resultType, final String reason) {
	        super(reason);
	        this.resultType = resultType;
        }
        public EndTest(final ResultType resultType, final String reason, final Throwable cause) {
            super(reason, cause);
            this.resultType = resultType;
        }
    }

	private Result currentResult;
	private HashMap<String, TestSpecification.Sub> subs;
	private LinkedList<TestSpecification.Sub> subsList;
	private int nextInt = 0;

	private boolean isTerminal = false;

	private Context currentContext;

	// Use if not null.
	private Logger logger;


	// ===============================================================================
	// = METHODS

	/**
	 * Construct the test.
	 */
	public TestSpecification() {
		super();
		currentResult = Result.newResult(name());
		subs = new HashMap<String, Sub>();
		subsList = new LinkedList<Sub>();

        List<Method> methodList = new ArrayList<Method>();

		for (Method method : this.getClass().getDeclaredMethods()) {

			if (method.isAnnotationPresent(TestCase.class)) {

                Sub sub = new Sub(method);
                subs.put(sub.name, sub);
                subsList.add(sub);
			}

		}

        Collections.sort(subsList, (a, b) -> b.compareTo(a));
	}

	// ===============================================================================
	// = INTERFACE

	/**
	 * Run the group.  In most cases, you would never call this directly.
	 * @param namespace current namespace for the test.  Allows organization.
	 * @param context current context.
     * @param target test target.  This doesn't have to be anything, but it is a convenient way to pass a target to the
     *            tests.
	 * @return the final Result
	 */
	public synchronized Result run(final String namespace, final Context context, final Object target) {

        currentContext = context;
		logger = PlatformInterfaceRequestor.getPlatformInterface().getLogger();
		if (! logger.isDebugging()) logger = null;

		String fqname = name();
        if ((namespace != null) && (namespace.length() > 0)) fqname = namespace + Test.NAMESPACE_SEPARATOR + fqname;

		currentResult = Result.start(name());

		if (logger != null) logger.debug("Start test specification", "testspec", fqname);
        _setup();

		boolean faulted = false;
		for (Sub sub : subsList) {

            Result result = Result.start(sub.name);
            ResultType rt = ResultType.INCONCLUSIVE;
            if (!faulted) {

                try {
                    sub.method.invoke(this);
                    rt = ResultType.PASSED;

                } catch (EndTest et) {
                    rt = et.resultType;
                    if (et.getCause() != null) result.fault(et.getCause());
                    result.message(et.getMessage());

                } catch (Exception e) {
                    rt = ResultType.FAULT;
                    result.fault(e);
                    result.message("Fault due to spurious exception.");
                }
            }

            faulted = rt.isFailedOrFailing();
            result.transition(rt).close();
            currentResult.addChild(result);

		}

		_teardown();
		currentResult.close();

		// If this is terminal group, fail it instead of faulting it.
		if (isTerminal && (currentResult.type == ResultType.FAULT)) currentResult.type = ResultType.FAILED;

		if (logger != null) logger.debug("... group done", "name", fqname, "result.type", currentResult.type.name());

		return currentResult;
	}


	// ===============================================================================
	// = SERVICES

    /**
     * Fault the test.  Execution of the test specification will end.
     * @param reason reason message.
     */
	protected void FAULT(final String reason) {
        throw new EndTest(ResultType.FAULT, reason);
	}

    /**
     * Fault the test.  Execution of the test specification will end.
     * @param reason reason message.
     * @param cause the cause.
     */
    protected void FAULT(final String reason, final Throwable cause) {
        throw new EndTest(ResultType.FAULT, reason, cause);
    }

    /**
     * Fail a test.  Execution of the test specification will continue.
     * @param reason reason message.
     */
    protected void FAIL(final String reason) {
        throw new EndTest(ResultType.FAILED, reason);
    }

    /**
     * Fail a test.  Execution of the test specification will continue.
     * @param reason reason message.
     * @param cause the cause.
     */
    protected void FAIL(final String reason, final Throwable cause) {
        throw new EndTest(ResultType.FAILED, reason);
    }

    /**
     * Pass the test.  Only call if you want to end the test early but still make it passed.  Tests that don't
     * call any of these methods will be considered passed when they complete the implementing method.
     * @param reason reason message.
     */
    protected void PASS(final String reason) {
        throw new EndTest(ResultType.PASSED, reason);
    }

    /**
     * Get the current context.
     * @return
     */
    protected Context CONTEXT() {
        return currentContext;
    }

    // ===============================================================================
    // = INTERNAL


}
