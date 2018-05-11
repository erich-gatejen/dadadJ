package dadad.process.data.wf;

import dadad.data.DataContext;
import dadad.data.model.Block;
import dadad.data.model.Result;
import dadad.platform.ContextRunnable;
import dadad.platform.config.ConfigurationType;
import dadad.process.WorkflowStep;

public class WFSTest extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS
	
	private ContextRunnable test;
	private String name;

	
	// ===============================================================================
	// = INTERFACE
	
	public boolean _takesInterspace() {
		return false;
	}

	
	// ===============================================================================
	// = ABSTRACT
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{ };
		
	}
	
	protected ConfigurationType[] _required() {
		return new ConfigurationType[] { };
	}	
	
	public void _start() {
		// NOP
	}
	
	public Block _step(Block block) {		

		try {
			block.result = test.run(name, getContext(), block);
			
		} catch (Exception e) {
			block.result = Result.fault(name, e);
		}
		
		if (block.result.type.isFailed()) return null;
		return block;
	}

	protected void _end() { 
		// NOP
	}
	
	protected void _close() {
		// NOP
	}
	
	
	// ===============================================================================
	// = METHODS
	
	public WFSTest() {
		throw new Error("BUG! BUG! BUG!  Do not use this workflow step directly.");
	}
	
	public WFSTest(final ContextRunnable test, final String name) {
		this.test = test;
		this.name = name;
	}
	
	
	// ===============================================================================
	// = INTERNAL
	
	
}
