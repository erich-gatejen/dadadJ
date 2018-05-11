package dadad.system.api;

import dadad.platform.Context;
import dadad.system.WorkKernel;

/**
 * API implementation.  This is NOT necessary for an API, but it enables persistence.
 */
public abstract class APIImpl {
	
	// ===============================================================================
	// = FIELDS
	
	protected Context context;
	
	// ===============================================================================
	// = ABSTRACT
	
	abstract public long persistanceLimit();

	// ===============================================================================
	// = METHODS
	
	public APIImpl() {
		super();
		reset();
	}
		
	public void reset() {
		context = WorkKernel.getSystemInterface().getContext();
	}
	
}
