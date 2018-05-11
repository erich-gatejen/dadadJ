package dadad.process.data.wf;

import dadad.data.DataContext;
import dadad.data.config.DataConfiguration;
import dadad.data.model.Block;
import dadad.data.model.Term;
import dadad.platform.AnnotatedException;
import dadad.platform.config.ConfigurationType;
import dadad.platform.config.ContextConfiguration;
import dadad.process.WorkflowStep;

public class WFSTermAlteration extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS

	private boolean alterRaw;
	private String runToken;
	private int[] termsToAlterByPosition;
	
	
	// ===============================================================================
	// = INTERFACE
	
	public boolean _takesInterspace() {
		return false;
	}
	
	
	// ===============================================================================
	// = ABSTRACT
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{ DataConfiguration.class };		
	}
	
	protected ConfigurationType[] _required() {
		return new ConfigurationType[] { ContextConfiguration.CONTEXT_RUN };
	}
	
	protected void _start() {		
		alterRaw = getConfig().getBoolean(DataConfiguration.ALTER_RAW);				
		runToken = getConfig().getRequired(ContextConfiguration.CONTEXT_RUN).trim();
		if (runToken.length() < 1) throw new AnnotatedException("Altering data requires a run uniquification token.  See the property to a non-empty string")
			.annotate("property.name", ContextConfiguration.CONTEXT_RUN.property());		
		
		termsToAlterByPosition = getConfig().getIntMultivalue(DataConfiguration.ALTER_TERMS_POSITION);			
	}
	
	protected void _end() { 
		// NOP
	}
	
	protected void _close() {
		// NOP
	}
	
	protected Block _step(Block block) {

		if (termsToAlterByPosition != null) {

			String specificRunToken = runToken + block.info.blockId();
			Term[] termsOriginal = block.getTerms();
			String[] termsText = block.getTermText();
			
			for (int index = 0; index < termsToAlterByPosition.length; index++) {
				int termBeingAltered = termsToAlterByPosition[index];
				termsText[termBeingAltered] = termsOriginal[termBeingAltered].text + specificRunToken;
			}
			
			block.alterTerms(alterRaw, termsText);
		}
		
		return block;
	}	
	
}
