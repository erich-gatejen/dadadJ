package dadad.data.store;

import dadad.data.model.BlockInfo;
import dadad.data.model.Term;

/**
 * Term writer interface.
 */
public interface TermWriter  {	
	
	// ===============================================================================
	// = FIELDS
	
	
	// ===============================================================================
	// = METHODS
	
	public void submit(BlockInfo blockInfo, String tag, Term[] terms);
	
}