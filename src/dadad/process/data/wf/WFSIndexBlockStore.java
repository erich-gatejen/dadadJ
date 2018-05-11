package dadad.process.data.wf;

import dadad.data.DataContext;
import dadad.data.model.Block;
import dadad.data.store.TermIndexWriter;
import dadad.data.store.Terms2BlockStore;
import dadad.platform.config.ConfigurationType;
import dadad.process.WorkflowStep;

public class WFSIndexBlockStore extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS

	private TermIndexWriter termIndexWriter;
	private Terms2BlockStore terms2BlockStore;
	
	
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
		return null;
	}	
	
	public void _start() {
		if (termIndexWriter == null) {
			termIndexWriter = new TermIndexWriter(getContext());
			termIndexWriter.open();
		}
		if (terms2BlockStore == null) {
			terms2BlockStore = new Terms2BlockStore(getContext());
		}		

	}
	
	public Block _step(Block block) {		
		termIndexWriter.submit(block.info, null, block.getTerms());
		terms2BlockStore.submit(block.info, null, block.getTerms());
		return block;
	}

	public void _end() {
		// close();
	}
	
	public void _close() {
		try {
			termIndexWriter.close();
		} catch (Throwable  t) {
			// Dont care
		}
		termIndexWriter = null;

		try {
			terms2BlockStore.close();
		} catch (Throwable t) {
			// Dont care
		}
		terms2BlockStore = null;
	}
	
	// ===============================================================================
	// = METHODS
	
}
