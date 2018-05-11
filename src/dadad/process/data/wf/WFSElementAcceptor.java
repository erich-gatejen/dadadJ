package dadad.process.data.wf;

import dadad.data.DataContext;
import dadad.data.config.AcceptanceConfiguration;
import dadad.data.model.Block;
import dadad.data.model.Element;
import dadad.data.model.ElementType;
import dadad.data.model.Term;
import dadad.platform.RNG;
import dadad.platform.config.ConfigurationType;
import dadad.process.WorkflowStep;

public class WFSElementAcceptor extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS

	private int chanceToAcceptElement;
	private final RNG rng = new RNG();
	private int acceptFirst;
	
	private boolean[] acceptByPosition;
	
	
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
		chanceToAcceptElement = getConfig().getBoundedInt(AcceptanceConfiguration.CHANCE_TO_ACCEPT_ELEMENT, 0, Integer.MAX_VALUE);				
		acceptFirst = getConfig().getInt(AcceptanceConfiguration.ACCEPT_FIRST_NUMBER);
		
		int[] acceptByPositionConfig = getConfig().getIntMultivalue(AcceptanceConfiguration.ACCEPT_BY_POSITION);
		if ((acceptByPositionConfig != null) && (acceptByPositionConfig.length > 0)) {
			acceptByPosition = new boolean[acceptByPositionConfig[acceptByPositionConfig.length -1] + 1];
			if (acceptByPositionConfig.length > 0) {
				for (int position : acceptByPositionConfig) {
					acceptByPosition[position] = true;
				}			
			} 
			
		} else {
			acceptByPosition = null;	
		}
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
		}
		
		Term[] terms = block.getTerms();
		
		// No elements were accepted, so reject the block.
		if (terms.length == 0) {
			return null;
		}
		
		// Trivial dismiss
		if (chanceToAcceptElement <= 1) {
		
			for (int index = 0; index < terms.length; index++) {
				
				if (
						// It NOT accepted
						! (
								// Accept by position
								((acceptByPosition != null) && (index < acceptByPosition.length) && (acceptByPosition[index])) 
								
								||
								
								// Accept by chance
								(
										(chanceToAcceptElement != 0) &&
										(
												(chanceToAcceptElement == 1) ||
												(rng.rng.nextInt(chanceToAcceptElement + 1) == chanceToAcceptElement)
										)
										
								)		
						)
						
				) {
					
					if (terms[index].element == null) terms[index].element = Element.rejectedElement;
					else terms[index].element.type = ElementType.REJECTED;
				}
				
			} // end for
		}
		
		return block;
	}
	
}
