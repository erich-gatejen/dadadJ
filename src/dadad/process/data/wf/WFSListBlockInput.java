package dadad.process.data.wf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import dadad.data.DataContext;
import dadad.data.config.ListerConfiguration;
import dadad.data.list.ListerCatalog;
import dadad.data.list.ListerInput;
import dadad.data.model.Block;
import dadad.platform.AnnotatedException;
import dadad.platform.config.ConfigurationType;
import dadad.process.WorkflowStep;

public class WFSListBlockInput extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS

	private Reader reader;
	private ListerInput lister;
	
	
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
	
	public void _start() {
		
		// Hardcoded for file
		String listFilePath = getConfig().getRequired(ListerConfiguration.LIST_FILE);
		try {
			reader = new BufferedReader(new FileReader(listFilePath));
		} catch (IOException ie) {
			throw new AnnotatedException("Failed to open list file for writing", ie).annotate("file.path", listFilePath);
		}
		
		ListerCatalog listerEntry = ListerCatalog.getListerEntry(reader);
		lister = listerEntry.getInputLister(reader);
	}
	
	public Block _step(Block block) {		
		return lister.getBlock(block.info.ownerId());
	}

	public void _end() {
		close();
	}
	
	public void _close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (Exception e) {
				// Don't care
			}
			reader = null;
		}

	}
	
	// ===============================================================================
	// = METHODS
	
}
