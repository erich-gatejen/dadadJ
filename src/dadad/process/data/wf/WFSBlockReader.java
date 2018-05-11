package dadad.process.data.wf;

import static dadad.platform.AnnotatedException.Catagory.FAULT;

import java.io.BufferedReader;
import java.io.Reader;

import dadad.data.DataContext;
import dadad.data.config.WorkflowConfiguration;
import dadad.data.io.BlockReader;
import dadad.data.model.Block;
import dadad.data.model.Document;
import dadad.platform.AnnotatedException;
import dadad.platform.UrlDataFactory;
import dadad.platform.config.Configuration;
import dadad.platform.config.ConfigurationType;
import dadad.process.WorkflowStep;

public class WFSBlockReader extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS
	private BlockReader blockReader;
	private BufferedReader reader;
	
	public enum WFSBRConfiguration implements ConfigurationType {
		
		DOCUMENT_URL( WFSBlockReader.class.getName() + ".url");
		
		// == BOILERPLATE - internal only - don't touch ======================
	    private final String property;
	    private WFSBRConfiguration(final String property) {
	        this.property = property;
	    }
		public String property() {
			return property;
		}	
	}
	
	// ===============================================================================
	// = INTERFACE
	
	public boolean _takesInterspace() {
		return false;
	}
	
	// ===============================================================================
	// = ABSTRACT
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{ WorkflowConfiguration.class, WFSBRConfiguration.class };
		
	}
	
	protected ConfigurationType[] _required() {
		return new ConfigurationType[] { WFSBRConfiguration.DOCUMENT_URL};
	}
	
	protected void _start() {
		
		reader = UrlDataFactory.getReader(getConfig().getRequired(WFSBRConfiguration.DOCUMENT_URL));
		
		// Block Reader
		Class<?> blockReaderClass;
		String blockReaderClassName = getConfig().getRequired(WorkflowConfiguration.BLOCK_READER);
		try {
			blockReaderClass = Class.forName(blockReaderClassName);
			if (! blockReaderClass.getSuperclass().getCanonicalName().contains("BlockReader")) 
				throw new Exception("Class is not a subslass of BlockReader.");	
		} catch (Exception e) {
			throw new AnnotatedException("Could not create blockreader class", FAULT, e).annotate("class.name", blockReaderClassName);
		}		
		this.blockReader = getBlockReaderInstance(blockReaderClass, reader, getContext().currentDoc);
				
	}
	
	protected void _end() { 
		close();
	}
	
	protected void _close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (Exception e) {
				// Don't care
			}
			reader = null;
		}
	}
	
	protected Block _step(Block block) {		
		return blockReader.read();
	}
		
	// ===============================================================================
	// = INTERNAL
	
	private BlockReader getBlockReaderInstance(final Class<?> blockReaderClass, final Reader inputReader, final Document source) {
		try {
			return (BlockReader) blockReaderClass.getConstructor(Reader.class, Document.class, Configuration.class)
					.newInstance(inputReader, source, getConfig());
		} catch (Exception e) {
			throw new AnnotatedException("Could not create BlockReader.", FAULT, e)
				.annotate("class.name", blockReaderClass.getName());
		}
	}
	
}
