package dadad.process.data.wf;

import dadad.data.DataContext;
import dadad.data.config.AcceptanceConfiguration;
import dadad.data.model.Block;
import dadad.platform.RNG;
import dadad.platform.config.ConfigurationType;
import dadad.process.WorkflowStep;

public class WFSBlockAcceptor extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS
	
	private int chanceToAcceptBlock;
	private int acceptFirst;
	private final RNG rng = new RNG();
	
	// ===============================================================================
	// = METHOD
	
	
	// ===============================================================================
	// = INTERFACE
	
	public boolean _takesInterspace() {
		return false;
	}
	
	// ===============================================================================
	// = ABSTRACT
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{ AcceptanceConfiguration.class };
		
	}
	
	protected ConfigurationType[] _required() {
		return null;
	}
	
	protected void _start() {		
		chanceToAcceptBlock = getContext().getConfig().getBoundedInt(AcceptanceConfiguration.CHANCE_TO_ACCEPT_BLOCK, 0, Integer.MAX_VALUE);
		acceptFirst = getContext().getConfig().getInt(AcceptanceConfiguration.ACCEPT_FIRST_NUMBER);
	}
	
	protected void _end() { 
		// NOP
	}
	
	protected void _close() {
		// NOP
	}
	
	protected Block _step(Block block) {	
		if (acceptFirst > 0) {
			acceptFirst--;
			return block;
			
		} else if (chanceToAcceptBlock <= 1) {
			return block;
			
		} else {
			if (chanceToAcceptBlock == 1) return block;
			if (rng.rng.nextInt(chanceToAcceptBlock + 1) == chanceToAcceptBlock) return block;
		}	
		
		return null;
	}
	
	
}
