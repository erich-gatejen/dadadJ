package dadad.process.data.wf;

import dadad.data.DataContext;
import dadad.data.config.CSVConfiguration;
import dadad.data.config.FieldConfiguration;
import dadad.data.config.WorkflowConfiguration;
import dadad.data.manip.ElementForge;
import dadad.data.model.Block;
import dadad.data.model.ElementType;
import dadad.data.model.Term;
import dadad.platform.AnnotatedException;
import dadad.platform.config.ConfigurationType;
import dadad.process.WorkflowStep;

public class WFSCSVProcessor extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS

	private Term[] headerTerms = null;
	private ElementForge elementForge;
	private boolean headerIsPresent;

	// ===============================================================================
	// = INTERFACE
	
	public boolean _takesInterspace() {
		return false;
	}
	
	// ===============================================================================
	// = ABSTRACT
	
	private static Class<?>[] configs = new Class<?>[]{   WorkflowConfiguration.class, CSVConfiguration.class, FieldConfiguration.class };
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) configs;
	}
	
	protected ConfigurationType[] _required() {
		return null;
	}
	
	protected void _start() {
		elementForge = new ElementForge(getContext());
		headerIsPresent = getContext().getConfig().getBoolean(WorkflowConfiguration.HEADER_IS_PRESENT);
	}
	
	protected Block _step(Block block) {
		Block result = block;
		
		Term[] terms = block.getTerms();
		if (terms != null) {

			if ((block.info.isStartingBlock()) && headerIsPresent) {
						
				for (Term term : block.getTerms()) {
					term.set(ElementType.STRING, term.text, term.text);					
				}
				headerTerms = block.copyTerms();
				
			} else {
				
				// CVS line
				if (headerTerms == null) {
					for (int index = 0; index < terms.length; index++) {
						elementForge.set(terms[index], terms[index].text);
					}		
					
				} else {
					try {
						for (int index = 0; index < terms.length; index++) {
							elementForge.set(terms[index], headerTerms[index].text);
						}				
					} catch (ArrayIndexOutOfBoundsException aioobe) {
						throw new AnnotatedException("More elements than headers.", AnnotatedException.Catagory.ERROR)
								.annotate("headers.number", headerTerms.length, "term.number", terms.length);
					}
				}

			}		
			
		}
		
		return result;
	}

	public void _end() {
		getContext().currentDoc.terms = headerTerms;
	}
	
	public void _close() {
		// NOP
	}
	
	// ===============================================================================
	// = METHODS

	
}
