package dadad.data.list;

import java.io.Writer;

import dadad.data.model.Block;
import dadad.data.model.Element;
import dadad.platform.AnnotatedException;
import dadad.platform.Constants;
import dadad.platform.TabularEncoding;
import dadad.platform.config.Configuration;

/**
 * Tabular list format 1.
 * <p>
 * <code>
 * (ID_BLOCK_LABEL)     (Numeric:BockId)    (BlockType)     (...)
 * ...empty...          (ID_ELEMENT_LABEL)  (ElementType)   (String:tag)   (OPTIONAL: String:text)
 *
 * </code>
 *
 */
public class ListerOutputTabular1 implements ListerOutput, WritingLister, ListerCommon {
	
	// ===============================================================================
	// = FIELDS

	private Writer writer;
	private Block currentBlock;

	public final static int SPOT_BLOCKHEADER_LABEL = 0;
    public final static int SPOT_BLOCKHEADER_ORDINAL_ID = 1;
    public final static int SPOT_BLOCKHEADER_TYPE = 2;
    public final static int META__BLOCKHEADER_MINIMUM_SIZE = SPOT_BLOCKHEADER_TYPE + 1;

    public final static int SPOT_ELEMENT_LABEL = 1;
    public final static int SPOT_ELEMENT_TYPE = 2;
    public final static int SPOT_ELEMENT_TAG = 3;
    public final static int SPOT_ELEMENT_TEXT = 4;
    public final static int META__ELEMENT_MINIMUM_SIZE = SPOT_ELEMENT_TAG + 1;

    public final static int META__ENTRY_MINIMUM_SIZE = META__BLOCKHEADER_MINIMUM_SIZE;



    // ===============================================================================
	// = METHODS
	
	public ListerOutputTabular1() {
	}
	
	// ===============================================================================
	// = INTERFACE
	
	public void setTarget(final Writer writer) {
		this.writer = writer;
	}
	
	public void configure(final Configuration configuration) {
		// NOP
	}
	
	public String version() {
		return ListerCatalog.VERSION__TABULAR1.getVersion();
	}

	public void put(final Block block) {
		
		try {
			writer.write(
					TabularEncoding.encode(
							ID_BLOCK_LABEL,
							block.info.blockId(),
							block.info.type().name()
						)
				);
			writer.write(Constants.NEWLINE);
		
		} catch (Exception e) {
			throw new AnnotatedException("Failed to write block due to IO exception.  This lister is permenately failed.", e);
		}		
		currentBlock = block;
	}

	public void put(final Element element) {
		if (currentBlock == null) throw new Error("BUG! BUG! BUG!  You must call put(Block) before put(Element).");
		
		try {
			writer.write(
					TabularEncoding.encode(
							"",
							ID_ELEMENT_LABEL,
							element.type.name(),
							element.tag,
							element.text						
						)
				);
			writer.write(Constants.NEWLINE);
		
		} catch (Exception e) {
			throw new AnnotatedException("Failed to write block due to IO exception.  This lister is permenately failed.", e);
		}			
		
	}
	
	public boolean optionBlockElementsAreSaved() {
		return false;
	}
}
