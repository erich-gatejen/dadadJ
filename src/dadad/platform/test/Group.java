package dadad.platform.test;

import java.util.HashMap;
import java.util.LinkedList;

import dadad.data.model.Result;
import dadad.data.model.ResultType;
import dadad.platform.Context;
import dadad.platform.ContextRunnable;
import dadad.platform.services.Logger;
import dadad.platform.services.PlatformInterfaceRequestor;

/**
 * A test.
 * <br>
 * A call to run (interface ContextRunnable) will run the test.  In terms of the RESULT, 
 * each call to this must be completely stateless.  Any test can be executed any arbitrary number 
 * of times with whatever context configurations.   The system, context or whatever may keep as much state as it wants.
 * Any return will be considered a pass.  Any exception will be a fault.   Calls to 
 * helper methods below will act accordingly.
 * 
 */
public abstract class Group implements TestRunnable {
	
	// ===============================================================================
	// = ABSTRACT
	
	/**
	 * Define the group by making calls to the DEFINITION METHODS (or not).
	 */
	abstract public void _define();
	
	
	// ===============================================================================
	// = FIELDS
	
	class Sub { 
		String name;
		Result result;
		ContextRunnable runnable;
		public Sub(final String name, final ContextRunnable runnable) {
			this.name = name;
			this.result = null;
			this.runnable = runnable;
		}
	}
	
	private Result currentResult;	
	private HashMap<String, Sub> subs;
	private LinkedList<Sub> subsList;
	private int nextInt = 0;
	
	private boolean isTerminal = false;
	
	// Use if not null.
	private Logger logger;
	
	
	// ===============================================================================
	// = DEFINITION METHODS
	
	public void add(final TestRunnable test) {
		nextInt++;
		if (subs.containsKey(test.name())) throw new Error("BUG: Test by name " + test.name() + " already added to " + name());
		add(test.name(), test);
	}
	
	public void add(final Group group) {
		nextInt++;
		if (subs.containsKey(group.name())) throw new Error("BUG: Group by name " + group.name() + " already added to " + name());
		add(group.name(), group);
	}	
	
	public void add(final ContextRunnable runnable) {
		nextInt++;
		add(Integer.toString(nextInt), runnable);
	}
	
	private void add(final String name, final ContextRunnable runnable) {
		Sub sub = new Sub(name, runnable);
		subs.put(Integer.toString(nextInt), sub);
		subsList.add(sub);		
	}
	
	/**
	 * Set the group as terminal.  It will not propagate faults from here, though the result will obviously
	 * not be a PASS.
	 */
	public void isTerminal() {
		isTerminal = true;
	}
	
	
	// ===============================================================================
	// = METHODS

	/**
	 * Construct the test. 
	 */
	public Group() {
		super();
		currentResult = Result.newResult(name());
		subs = new HashMap<String, Sub>();
		subsList = new LinkedList<Sub>();
		
		_define();
	}

	/**
	 * Construct the test with given name.  This should only be used within the package.
	 */
    Group(final String name) {
		this();

		// Override the result with a named one.
		currentResult = Result.newResult(name);
	}

	/**
	 * Run the group.  In most cases, you would never call this directly.
	 * @param namespace current namespace for the test.  Allows organization.
	 * @param context current context.
	 * @return the final Result
	 */
	public Result run(final String namespace, final Context context, final Object target) {
		
		logger = PlatformInterfaceRequestor.getPlatformInterface().getLogger();
		if (! logger.isDebugging()) logger = null;
		
		String fqname = name();
		if ((namespace != null) && (namespace.length() > 0)) fqname = namespace + Test.NAMESPACE_SEPARATOR + fqname;
		
		currentResult = currentResult.start();
		
		if (logger != null) logger.debug("Start group", "group", fqname);
		
		boolean faulted = false;
		for (Sub sub : subsList) {
			
			try {

				String sname = fqname + Test.NAMESPACE_SEPARATOR + sub.name;
				if (faulted) {
					if (logger != null) logger.debug(">>> group sub NOT run due to previous fault", "name", sname,
							"class", sub.runnable.getClass().getName());					
					if (sub.runnable instanceof Group) sub.result = ((Group) sub.runnable).faultGroup(namespace);
					else sub.result = new Result(sname, "Previous fault in group.", ResultType.INCONCLUSIVE);
					
					if (sub.result.type.isFailedOrFailing()) currentResult.type = ResultType.FAILING;
					
				} else {
					if (logger != null) logger.debug(">>> group sub run", "name", fqname  + Test.NAMESPACE_SEPARATOR + sub.name,
							"class", sub.runnable.getClass().getName());
					sub.result = sub.runnable.run(namespace, context, target);
					if (logger != null) logger.debug(">>> >>> group sub result", "name", sname,
							"result.type", sub.result.type.name());				
					if (sub.result.type == ResultType.FAULT) faulted = true;  // Abandon the rest.
				}
						
			} catch (Exception e) {
				// DO NOT LET EXCEPTIONS OUT!
				throw new Error("BUG BUG BUG!!!  An exception escaped a Test/Group/ContextRunnable for Group run.");
			}
				
		}
		
		for (Sub sub : subsList) {
			currentResult.addChild(sub.result);			
		}
		currentResult.close();
		
		// If this is terminal group, fail it instead of faulting it.
		if (isTerminal && (currentResult.type == ResultType.FAULT)) currentResult.type = ResultType.FAILED; 
		
		if (logger != null) logger.debug("... group done", "group", fqname, "result.type", currentResult.type.name());		
					
		return currentResult;
	}

	public int numberOfChildren() {
		return subsList.size();
	}

	public ContextRunnable peekChild(final int index) {
	    if (subsList.size() <= index) return null;
	    Sub sub = subsList.get(index);
	    return sub.runnable;
    }

	// ===============================================================================
	// = INTERNAL
		
	private Result faultGroup(final String namespace) {
		
		String fqname = name();
		if (namespace != null) fqname = namespace + Test.NAMESPACE_SEPARATOR + fqname;	
		
		for (Sub sub : subsList) {

			String sname = fqname + Test.NAMESPACE_SEPARATOR + sub.name;
			if (sub.runnable instanceof Group) sub.result = ((Group) sub.runnable).faultGroup(fqname);
			else sub.result = new Result(sname, "Previous fault in group.", ResultType.INCONCLUSIVE);
			
			sub.result.metric.start().done();			
		}
		
		Result pendingResult = Result.newResult(fqname);
		for (Sub sub : subsList) {			
			pendingResult.addChild(sub.result);			
		}
		pendingResult.resolve();
		
		return pendingResult;
	}
	
}
