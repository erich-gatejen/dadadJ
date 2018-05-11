package dadad.system;

import java.io.Serializable;

import dadad.data.model.Result;

public class WorkProcessInfo implements Serializable {
		
	private static final long serialVersionUID = 1L;
	
	// Rarely mutable 
	public int id;
	public String name;
	
	// Easily mutable
	public volatile WorkProcessState state = WorkProcessState.GENESIS;
	public volatile Result result;
	
}
