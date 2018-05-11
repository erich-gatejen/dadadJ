package dadad.platform.test;

import dadad.data.model.Block;
import dadad.platform.Context;

/**
 * A block wrapper for Test
 */
public abstract class BlockTest extends Test {
	
	// ===============================================================================
	// = ABSTRACT

	
	/**
	 * Run the test.
	 * @param context test context.
	 * @param target test target.
	 */
	abstract public void _run(final Context context, final Block target);
	

	// ===============================================================================
	// = METHODS

	public void _run(final Context context, final Object target) {
		try {
			_run(context, (Block) target);
		} catch (ClassCastException cce) {
			throw new Error("BlockTest can only accept BLock objects as the target.", cce);
		}
	}
	
	
}
