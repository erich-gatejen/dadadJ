package dadad.platform.test;

import dadad.data.model.Result;
import dadad.platform.Context;
import dadad.platform.ContextRunnable;

/**
 * A runnable test interface.
 */
public interface TestRunnable extends ContextRunnable {

    String name();
}
