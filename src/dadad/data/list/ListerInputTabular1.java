package dadad.data.list;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;

import dadad.data.model.*;
import dadad.platform.AnnotatedException;
import dadad.platform.TabularEncoding;
import dadad.platform.config.Configuration;
import dadad.platform.config.ConfigurationType;

/**
 * Tabular list format 1.
 * <b></b>
 * See {@link ListerOutputTabular1} for formatting rules.
 */
public class ListerInputTabular1 implements ListerInput, ReadingLister {
	
	// ===============================================================================
	// = FIELDS
	
	private BufferedReader reader;
	private String[] current;
	private int lineNumber;
	
	
	// ===============================================================================
	// = METHODS
	
	public ListerInputTabular1() {
		super();
	}
	
	// ===============================================================================
	// = INTERFACE
	
	public void configure(final Configuration configuration) {
		// NOP
	}

	public String version() {
		return ListerCatalog.VERSION__TABULAR1.getVersion();		
	}
	
	public void setTarget(final Reader reader) {
		if (reader instanceof BufferedReader)
			this.reader = (BufferedReader) reader;
		else
			this.reader = new BufferedReader(reader);
		lineNumber = 0;
		tee();
	}
	
	public Block getBlock(final long ownerId) {
		Block block = null;
		
		try {
			if (current == null) return null;
			if (current.length < ListerOutputTabular1.META__BLOCKHEADER_MINIMUM_SIZE) throw new Exception("Not enough elements for a Block Header");
			if (! current[ListerOutputTabular1.SPOT_BLOCKHEADER_LABEL].equals(ListerOutputTabular1.ID_BLOCK_LABEL))  throw new Exception("Expecting a Block Header to start block.");
			
			// Block info
			long blockOrdinalId = getLong(current[ListerOutputTabular1.SPOT_BLOCKHEADER_ORDINAL_ID], "blockId");
			BlockType blockType = (BlockType) getEnumObject(current[ListerOutputTabular1.SPOT_BLOCKHEADER_TYPE], BlockType.class,"block " +
                    "type");
			BlockInfo blockInfo = new BlockInfo(blockType, ownerId, DataId.getBlockId(ownerId, blockOrdinalId));
			
			ArrayList<Term> terms = new ArrayList<Term>();
			while(true) {
				tee();
				if (current == null) break;
				if (current.length < (ListerOutputTabular1.META__ENTRY_MINIMUM_SIZE)) throw new Exception("Truncated line.");
				if (current[ListerOutputTabular1.SPOT_BLOCKHEADER_LABEL].equals(ListerOutputTabular1.ID_BLOCK_LABEL)) break;	// Next block
				if (current.length < (ListerOutputTabular1.META__ELEMENT_MINIMUM_SIZE)) throw new Exception("Not enough line attributes to be an Element.");

				if (! current[ListerOutputTabular1.SPOT_ELEMENT_LABEL].equals(ListerOutputTabular1.ID_ELEMENT_LABEL))  throw new Exception("Expecting a an Element.");
				ElementType elementType = (ElementType) getEnumObject(current[ListerOutputTabular1.SPOT_ELEMENT_TYPE], ElementType.class,"element type");
				
				Term term;
				if (current.length > (ListerOutputTabular1.META__ELEMENT_MINIMUM_SIZE)) {
                    term = new Term(current[ListerOutputTabular1.SPOT_ELEMENT_TEXT]).set(elementType, current[ListerOutputTabular1.SPOT_ELEMENT_TAG]);
				} else {
                    // Empty last element
                    term = new Term("").set(elementType, current[ListerOutputTabular1.SPOT_ELEMENT_TAG]);
				}
				terms.add(term);
			}
			
			block = new Block(blockInfo, null, terms.toArray(new Term[terms.size()]));			
				
		} catch (AnnotatedException ae) {
			throw ae;
		} catch (Exception e) {
			throw new AnnotatedException("Failed list reading due to a list problem.", AnnotatedException.Catagory.ERROR)
				.annotate("reason", e.getMessage(), "line.number", lineNumber);
		}
		
		return block;		
	}
	
	/**
	 * Queries if the implementation if the original Block terms are present in the blocks.
	 * @return true if they are present, otherwise false.
	 */
	public boolean optionBlockTermsArePresent() {
		return false;
	}
	
	// ===============================================================================
	// = INTERNAL
		
	private void tee() {
		try {
			current = null;
			
			do {
				String line = this.reader.readLine();
				if (line == null) {
					current = null;
					return;
				}
				lineNumber++;
				if (line.trim().startsWith(ListerOutputTabular1.COMMENT_CHARACTER)) continue;
				
				current = TabularEncoding.decode(line);
				
			} while (current == null);

		} catch (Exception e) {
			throw new AnnotatedException("Failed to read from list", AnnotatedException.Catagory.FAULT, e)
				.annotate("line.number", lineNumber);
		}		
	}
	
	private long getLong(final String data, final String name) {
		try {
			return Long.parseLong(data);
		} catch (Exception e) {
			throw new AnnotatedException("Bad numeric value for " + name, AnnotatedException.Catagory.ERROR)
				.annotate("reason", e.getMessage(), "line.number", lineNumber);
		}
	}
	
	private Object getEnumObject(final String data, final Class<?> clazz, final String name) {
		try {
			@SuppressWarnings("unchecked")
			Enum<?> value = ConfigurationType.findEnumValue((Class<? extends Enum<?>>) clazz, data.toString());
			if (value == null) {
				throw new Exception("Not a valid enum value for enum " + clazz.getName());
			}
			return value;
			
		} catch (Exception e) {
			throw new AnnotatedException("Bad numeric value for " + name, AnnotatedException.Catagory.ERROR)
				.annotate("reason", e.getMessage(), "line.number", lineNumber);
		}
	}

}
