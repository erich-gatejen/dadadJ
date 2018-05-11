package dadad.process.data.wf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import dadad.data.DataContext;
import dadad.data.config.ListerConfiguration;
import dadad.data.list.ListerCatalog;
import dadad.data.list.ListerOutput;
import dadad.data.model.Block;
import dadad.data.model.Term;
import dadad.platform.AnnotatedException;
import dadad.platform.config.ConfigurationType;
import dadad.process.WorkflowStep;

public class WFSLister extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS

	private Writer writer;
	private ListerOutput lister;

	
	// ===============================================================================
	// = INTERFACE
	
	public boolean _takesInterspace() {
		return false;
	}
	
	
	// ===============================================================================
	// = ABSTRACT
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{ ListerConfiguration.class };
		
	}
	
	protected ConfigurationType[] _required() {
		return new ConfigurationType[] { ListerConfiguration.LIST_FILE };
	}	
	
	protected void _start() {
		
		// Hardcoded for file
		String listFilePath = getConfig().getRequired(ListerConfiguration.LIST_FILE);
		try {
			
			writer = new BufferedWriter(new FileWriter(listFilePath));
				
		} catch (IOException ie) {
			throw new AnnotatedException("Failed to open list file for writing", ie).annotate("file.path", listFilePath);
		}
		
		ListerCatalog listerEntry = ListerCatalog.getListerEntry(getConfig().getRequired(ListerConfiguration.LIST_TYPE));
		lister = listerEntry.getOutputLister(writer);
	}
	
	protected Block _step(Block block) {		
		lister.put(block);
		Term[] terms = block.getTerms();
		for (int index = 0; index < terms.length; index++) {
			lister.put(terms[index].element);
		}		
		return block;
	}

	protected void _end() {
		close();
	}
	
	protected void _close() {
		if (writer != null) {
			try {
				writer.close();
			} catch (Exception e) {
				// Don't care
			}
			writer = null;
		}

	}
	
	// ===============================================================================
	// = METHODS
	
}
