package dadad.platform;

import dadad.data.model.Result;

/**
 * Runnable with a context.
 */
public interface ContextRunnable {
	
	/**
	 * Run.  It should be statefree between runs, but the context state may change however desired.
	 * @param namespace leading namespace
	 * @param context the context.
	 * @param target test target.
	 */
	public Result run(final String namespace, final Context context, final Object target);
	
}

