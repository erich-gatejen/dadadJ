package dadad.process;

import dadad.platform.Context;
import dadad.platform.config.Configurable;
import dadad.platform.config.Configuration;
import dadad.platform.config.ConfigurationType;

/**
 * Workflow steps.
 * 
 * NO configuration or processing should be included in ctors.  In fact, avoid ctors.  Instead,
 * do it in _start().
 * 
 * @param <E>
 * @param <C>
 */
public abstract class WorkflowStep<E, C extends Context> implements Configurable, WorkflowBlockHandler {
	
	// ===============================================================================
	// = ABSTRACT
	
	/**
	 * Get a list of configurations that must be present before workflow starts.
	 * @return An array of required configurations or null if there are none.
	 */
	abstract protected ConfigurationType[] _required();
	
	/**
	 * To be performed once before step()s.  If it throws an exception, the entire workflow will be aborted.  It is up to the
	 * user to make sure dependencies between WorkflowStep instances are resolved and properly ordered.
	 */
	abstract protected void _start();
	
	/**
	 * To be called for every available data of type E.
	 * @param data
	 * @return null if the data is rejected by this step for reasons that are recoverable or intentional.  If rejected, the E may be forwarded 
	 *      to another step (it will have any changes made during the failed step).  
	 */
	abstract protected E _step(E data);
	
	/**
	 * To be performed once after step()s.  If it throws an exception, the entire workflow will be aborted.
	 */	
	abstract protected void _end();	
	
	/**
	 * To be called for every WorkflowStep no matter what.  This should clean up resources after
	 * success or failure of running the workflow.  It is important that this method is idempotent.  Once
	 * it is called, the WorkflowStep cannot be used again.
	 * 
	 * Do not let exceptions escape this method unless you want the whole processor framework 
	 * to fail.
	 */
	abstract protected void _close();	
		
	// ===============================================================================
	// = FIELDS

	private C context;
	private boolean closed = false;
	

	// ===============================================================================
	// = API
	// = Doing this so I can shim checks in later.
	
	public void start() {		
		_start();
	}
	
	@SuppressWarnings("unchecked")
	public E step(Object o) {
		
		// This exists because java generics suck
		try {
			return _step((E) o);
		} catch (ClassCastException cce) {
			throw new Error("BUG BUG BUG: Bad type passed as workflow parameter.");
		}	
	}
	
	public void end() {
		_end();
	}
	
	public void close() {
		_close();
	}
	
	
	// ===============================================================================
	// = METHODS
	
	public C getContext() {
		return context;
	}
	
	public Configuration getConfig() {
		return context.getConfig();
	}
	
	public WorkflowStep<E, C> set(C context) {
		this.context = context;
		return this;
	}

	
	// ===============================================================================
	// = INTERNAL
		
	boolean isClosed() {
		return closed;
	}
	
	void closeIfNotClosed() {
		if (! closed) { 
			close();
			closed = true;
		}
	}
	
}
