package dadad.process.data.wf;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;
import java.util.Map.Entry;

import dadad.data.DataContext;
import dadad.data.config.OutputConfiguration;
import dadad.data.model.Block;
import dadad.data.model.Term;
import dadad.platform.UrlDataFactory;
import dadad.platform.config.ConfigurationType;
import dadad.process.WorkflowStep;
import dadad.system.SystemInterface;
import dadad.system.WorkKernel;

public class WFSDump extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS
	
	public final static String SEPARATOR = "-------------------------------------------------------------------------";
	public final static String HEADER = "--------";
	public final static String TERM = "--";

	private BufferedWriter writer;
	private boolean trace;
	
	public enum WFSDumpBWConfiguration implements ConfigurationType {
		
		DESTINATION_URL( "__WFSDump.url");
		
		// == BOILERPLATE - internal only - don't touch ======================
	    private final String property;
	    private WFSDumpBWConfiguration(final String property) {
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
		return (Class<ConfigurationType>[]) new Class<?>[]{ WFSDumpBWConfiguration.class };
		
	}
	
	protected ConfigurationType[] _required() {
		return new ConfigurationType[] { WFSDumpBWConfiguration.DESTINATION_URL };
	}	
	
	public void _start() {
		writer = UrlDataFactory.getWriter(getConfig().getRequired(WFSDumpBWConfiguration.DESTINATION_URL),
				getConfig().getBoolean(OutputConfiguration.APPEND_DATA));
		
		// HAX
		try {
			writer.write('\ufeff');
		} catch (Exception e) {
			// derp
		} 
		
		SystemInterface si = WorkKernel.getSystemInterface();
		trace = si.getLogger().isTrace();
	}
	
	public Block _step(Block block) {		

		try {
			writer.write(SEPARATOR);
			writer.newLine();
			
			writer.write(HEADER);
			writer.write("blockinfo");
			writer.newLine();			
			writer.write("blockinfo.blockType :");
			writer.write(block.info.type().name());
			writer.newLine();
			writer.write("blockinfo.ownerId :");
			writer.write(Long.toString(block.info.ownerId()));
			writer.newLine();
			writer.write("blockinfo.blockId :");
			writer.write(Long.toString(block.info.blockId()));
			writer.newLine();
			writer.write("blockinfo.start :");
			writer.write(Long.toString(block.info.start()));
			writer.newLine();
			writer.write("blockinfo.end :");
			writer.write(Long.toString(block.info.end()));
			writer.newLine();
			
			writer.write(HEADER);
			writer.write("result");
			writer.newLine();
			if (block.result != null) {
				writer.write(block.result.toString());
				writer.newLine();
			}			
			
			writer.write(HEADER);
			writer.write("attributes");	
			writer.newLine();
			Set<Entry<String, Object>> attribsSet = block.getAllAttributes();
			if (attribsSet != null) {
				for (Entry<String, Object> aentry : attribsSet) {
					writer.write(aentry.getKey());
					writer.write(" :");
					writer.write(aentry.getValue().toString());
					writer.newLine();
				}
			}
			
			writer.write(HEADER);
			writer.write("terms");	
			writer.newLine();
			Term[] terms = block.getTerms();
			if (terms != null) {
				for (Term tentry : terms) {
					
					writer.write("term.text :");
					writer.write(tentry.text);
					writer.newLine();
					writer.write("term.start :");
					writer.write(Long.toString(tentry.start));
					writer.newLine();
					writer.write("term.end :");
					writer.write(Long.toString(tentry.end));
					writer.newLine();
					
					if (tentry.element != null) {
						writer.write("term.element.type :");
						writer.write(tentry.element.type.name());
						writer.newLine();					
						writer.write("term.element.tag :");
						writer.write(tentry.element.tag);
						writer.newLine();
						writer.write("term.element.text :");
						writer.write(tentry.element.text);
						writer.newLine();
					} else {
						writer.write("term.element.type : unknownq");
					}
					
					writer.write(TERM);
					writer.newLine();
					
				}
			}
			
			if (trace) {
				writer.write(HEADER);
				writer.write("raw");	
				writer.newLine();
				writer.write(block.raw);
				writer.newLine();				
			}
			
			writer.newLine();
		
		} catch (IOException ioe) {
			throw new RuntimeException("Failed to write to dump file.  " + ioe.getMessage(), ioe);
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
