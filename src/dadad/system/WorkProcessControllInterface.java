package dadad.system;

import dadad.data.model.Result;

public interface WorkProcessControllInterface {

	public WorkProcessState state();
	
	public void pause();
	
	public void unpause();
	
	public void halt();
	
	public Result getResult();
	
}
