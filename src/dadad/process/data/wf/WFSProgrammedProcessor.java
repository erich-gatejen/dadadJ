package dadad.process.data.wf;

import dadad.data.DataContext;
import dadad.data.config.FieldConfiguration;
import dadad.data.config.WorkflowConfiguration;
import dadad.data.manip.ElementForge;
import dadad.data.model.Block;
import dadad.data.model.ElementType;
import dadad.data.model.Term;
import dadad.platform.AnnotatedException;
import dadad.platform.config.ConfigurationType;
import dadad.process.WorkflowStep;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.LinkedList;

public class WFSProgrammedProcessor extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS

	private ProcessorProgram program;
	
	private HashSet<String> tags;
	
	private ElementForge elementForge;

	
	// ===============================================================================
	// = INTERFACE
	
	public boolean _takesInterspace() {
		return false;
	}
	
	
	// ===============================================================================
	// = ABSTRACT
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{FieldConfiguration.class, WorkflowConfiguration.class};
		
	}
	
	protected ConfigurationType[] _required() {
		return new ConfigurationType[] { WorkflowConfiguration.PROCESSOR__PROGRAM_FILE };
	}	
	
	public void _start() {
		
		elementForge = new ElementForge(getContext());
		
		String programPath = getConfig().getRequired(WorkflowConfiguration.PROCESSOR__PROGRAM_FILE);
		Reader programReader = null;
		try {
			try {
				programReader = new BufferedReader(new FileReader(programPath));
				program = new ProcessorProgram("process", programReader);
				
				if (program.instructions.length < 1) throw new AnnotatedException("Program has no instructions.");
			
			} catch (IOException ioe) {
				throw new AnnotatedException("Could not open processor program file.", AnnotatedException.Catagory.FAULT, ioe);
			}
		} catch (AnnotatedException e) {	
			throw e.annotate(WorkflowConfiguration.PROCESSOR__PROGRAM_FILE.property(), programPath);
			
		} finally {
			try {
				programReader.close();
			} catch (Exception e) {
				// NOP
			}
		}
		
		tags = new HashSet<String>();

	}
	
	public Block _step(Block block) {
	
		int ip = 0;
		int fieldPtr = 0;

		Term[] terms = block.getTerms();
		String[] termStrings = null;
		
		try {	
			for (ip = 0; ip < program.instructions.length; ip++) {
				
				if (fieldPtr >= terms.length) break;		// Out of fields.
				
				String term = terms[fieldPtr].text;
				
				switch(program.instructions[ip].instruction) {
				
				case STRING:					
					elementForge.set(ElementType.STRING, terms[fieldPtr]);
					fieldPtr++;
					break; 
					
				case TEXT:
					elementForge.set(ElementType.TEXT, terms[fieldPtr]);
					fieldPtr++;
					break;
					
				case FIELD:
					elementForge.set(ElementType.STRING, terms[fieldPtr], terms[fieldPtr].text);
					fieldPtr++;
					break;
					
				case NV:
					String value;
					int snap = term.indexOf('=');
					if ( (snap > 0) && (snap < (term.length() - 2)) ) {
						String tag = term.substring(0, snap);
						value = term.substring(snap + 1, term.length());
						elementForge.set(ElementType.STRING, terms[fieldPtr], tag);
						
						tags.add(tag);
						
						if (termStrings == null) {
							 termStrings = new String[terms.length];
							 for (int index = 0; index < fieldPtr; index++) {
									 termStrings[index] = terms[fieldPtr].text;
							 }
						}
						termStrings[fieldPtr] = value;
						
					}

					fieldPtr++;
					break;
				}
															
			} // end for
			
			// Have we altered the text?
			if (termStrings != null) {
				block.alterTerms(true, termStrings);
			}
		
		} catch (Exception e) {
			throw new AnnotatedException("Failed to execute process program.", AnnotatedException.Catagory.ERROR, e)
					.annotate("instruction.number", ip, "instruction", program.instructions[ip].instruction.name());
		}
		
		return block;				
	}
	
	public void _end() {
		LinkedList<Term> terms = new LinkedList<Term>();
		for (String tag : tags) {	
			terms.add(new Term(tag).set(ElementType.STRING, tag));
		} 
		getContext().currentDoc.terms = terms.toArray(new Term[terms.size()]);

	}
	
	public void _close() {
		// NOP
	}

	
	// ===============================================================================
	// = METHODS

}
