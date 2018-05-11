package dadad.system;

import dadad.data.model.Result;

public interface WorkProcessContainer {

	public void configure();
	
	public Result getCurrentResult();
	
	public Result run();
	
	public default void close() {
		// NOP
	}
}
