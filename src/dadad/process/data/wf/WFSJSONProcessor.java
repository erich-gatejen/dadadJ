package dadad.process.data.wf;

import dadad.data.DataContext;
import dadad.data.config.WorkflowConfiguration;
import dadad.data.manip.ElementTyper;
import dadad.data.model.Block;
import dadad.data.model.Term;
import dadad.platform.config.ConfigurationType;
import dadad.process.WorkflowStep;

import java.util.HashSet;

public class WFSJSONProcessor extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS

	private ElementTyper typer;
	private HashSet<String> termNameSet;
	
	// ===============================================================================
	// = INTERFACE
	
	public boolean _takesInterspace() {
		return false;
	}
	
	// ===============================================================================
	// = ABSTRACT
	
	private static Class<?>[] configs = new Class<?>[]{   WorkflowConfiguration.class };
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) configs;
		
	}
	
	protected ConfigurationType[] _required() {
		return null;
	}
	
	protected void _start() {
		typer = new ElementTyper(getContext());
		termNameSet = new HashSet<String>();
	}
	
	protected Block _step(Block block) {
		Block result = block;
		
		Term[] terms = block.getTerms();
		for (Term term : terms) {
			term.set(typer.type(term.element.text.trim()));
			termNameSet.add(term.element.tag);
		}
		
		return result;
	}

	public void _end() {
		
		Term[] nameTerms = new Term[termNameSet.size()];
		int index = 0;
		for (String name : termNameSet) {
			nameTerms[index] = new Term(name);			
			index++;		
		}
		getContext().currentDoc.terms = nameTerms;
	}
	
	public void _close() {
		// NOP
	}
	
	// ===============================================================================
	// = METHODS

	
}
