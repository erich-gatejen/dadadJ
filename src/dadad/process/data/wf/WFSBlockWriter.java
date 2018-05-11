package dadad.process.data.wf;

import static dadad.platform.AnnotatedException.Catagory.FAULT;

import java.io.BufferedWriter;
import java.io.Writer;

import dadad.data.DataContext;
import dadad.data.config.OutputConfiguration;
import dadad.data.config.WorkflowConfiguration;
import dadad.data.io.BlockWriter;
import dadad.data.model.Block;
import dadad.platform.AnnotatedException;
import dadad.platform.UrlDataFactory;
import dadad.platform.config.Configuration;
import dadad.platform.config.ConfigurationType;
import dadad.process.WorkflowStep;

public class WFSBlockWriter extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS
	
	private BlockWriter blockWriter;
	private BufferedWriter writer;
	
	public enum WFSBWConfiguration implements ConfigurationType {
		
		DESTINATION_URL( "__WFSBlockWriter.url");
		
		// == BOILERPLATE - internal only - don't touch ======================
	    private final String property;
	    private WFSBWConfiguration(final String property) {
	        this.property = property;
	    }
		public String property() {
			return property;
		}	
	}
	
	// ===============================================================================
	// = INTERFACE
	
	public boolean _takesInterspace() {
		return true;
	}
	
	// ===============================================================================
	// = ABSTRACT
	
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) new Class<?>[]{ WorkflowConfiguration.class, WFSBWConfiguration.class,
			OutputConfiguration.class};
		
	}
	
	protected ConfigurationType[] _required() {
		return new ConfigurationType[] { WFSBWConfiguration.DESTINATION_URL };
	}
	
	protected void _start() {
		
		writer = UrlDataFactory.getWriter(getConfig().getRequired(WFSBWConfiguration.DESTINATION_URL),
				getConfig().getBoolean(OutputConfiguration.APPEND_DATA));
		
		// HAX
		try {
			writer.write('\ufeff');
		} catch (Exception e) {
			// derp
		}
		
		// Block Reader
		Class<?> blockWriterClass;
		String blockWriterClassName = getConfig().getRequired(WorkflowConfiguration.BLOCK_WRITER);
		try {
			blockWriterClass = Class.forName(blockWriterClassName);
			if (! blockWriterClass.getSuperclass().getCanonicalName().contains("BlockWriter")) 
				throw new Exception("Class is not a subslass of blockWriter.");	
		} catch (Exception e) {
			throw new AnnotatedException("Could not create blockWriter class", FAULT, e).annotate("class.name", blockWriterClassName);
		}		
		this.blockWriter = getBlockWriterInstance(blockWriterClass, writer); 
				
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
	
	protected Block _step(Block block) {		
		blockWriter.write(block);
		return block;
	}
		
	// ===============================================================================
	// = INTERNAL
	
	private BlockWriter getBlockWriterInstance(final Class<?> blockWriterClass, final Writer outputWriter) {
		try {
			return (BlockWriter) blockWriterClass.getConstructor(Writer.class, Configuration.class)
					.newInstance(outputWriter, getConfig());
		} catch (Exception e) {
			throw new AnnotatedException("Could not create BlockWriter.", FAULT, e)
				.annotate("class.name", blockWriterClass.getName());
		}
	}
	
}
