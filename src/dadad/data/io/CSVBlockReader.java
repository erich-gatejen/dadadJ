package dadad.data.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import dadad.data.model.*;
import dadad.data.model.Document;
import dadad.platform.AnnotatedException;
import dadad.platform.config.Configuration;

public class CSVBlockReader extends BlockReader {
	
	// ===============================================================================
	// = FIELDS

	// Document Offsets
	private long sourceSpot;			// Current spot in the source
	private long sourceBlockMark;		// Mark in the source the start of the block
	
	// Term offsets
	private long termStartSpot;			// Start within the block where a term starts
	private long blockCursor;			// Current cursor position within a block
	
	private BufferedReader brin;
	
	// ===============================================================================
	// = METHODS
	
	/**
	 * Constructor.  IMPORTANT: this will not close the reader!
	 * @param inputReader
	 */
	public CSVBlockReader(final Reader inputReader, final Document source, final Configuration config) {
		super(inputReader, source, config);
		
		brin = new BufferedReader(getReader());
		termStartSpot = 0;
		sourceSpot = 0;
	}
	
	// ===============================================================================
	// = INTERFACE

	enum State {
		DONE,
		LEAD,
		OPEN,
		QUOTE,
		QUOTED,
		QUOTE_IN_QUOTE,	
	}
	
	private int readChar() throws IOException {
		blockCursor++;
		sourceSpot++;
		return brin.read();
	}
	
	protected Block readBlock(final long blockId, final long ownerId) {

		Block result = null;
		try {
			State state = State.LEAD;
			termStartSpot = 0;
		    blockCursor = -1;
			int current = -1;
			sourceBlockMark = sourceSpot;
			boolean started = false;
			
			StringBuilder raw = new StringBuilder();
			ArrayList<Term> terms = new ArrayList<Term>();
			while(terms.size() < 1) {
				StringBuilder accumulator = new StringBuilder();							
			
				while (state != State.DONE) {
					current = readChar();
					if (current < 0) break;
					
					switch(state) {
					
					case LEAD:
						switch(current) {
						case '\n':
						case '\r':
							// Leftover line term.  Eat it.
							break;
							
						case '"':
							raw.append((char) current);
							state = State.QUOTE;
							break;
							
						case ',':
							raw.append((char) current);
							if (accumulator.length() > 0) {
								terms.add(new Term(accumulator.toString(), blockCursor, blockCursor));
								accumulator = new StringBuilder();
							}
							break;
							
						default:
							raw.append((char) current);
							accumulator.append((char) current);
							state = State.OPEN;
							termStartSpot = blockCursor;
							started = true;
							break;	
							
						}
						break;
						
					case OPEN:
						switch(current) {
						case '"':
							raw.append((char) current);
							state = State.QUOTE;
							break;
							
						case ',':
							raw.append((char) current);
							if (accumulator.length() > 0) {
								terms.add(new Term(accumulator.toString(), termStartSpot, blockCursor));
								accumulator = new StringBuilder();
							} else {
								// Won't be started.
								terms.add(new Term(accumulator.toString(), blockCursor, blockCursor));
							}
							started = false;
							break;
							
						case '\n':
						case '\r':
							state = State.DONE;
							current = -1;
							break;
							
						default:
							if (! started) {
								termStartSpot = blockCursor;
								started = true;
							}
							raw.append((char) current);
							accumulator.append((char) current);
							break;						
						}
						break;
						
					case QUOTE:
						raw.append((char) current);
						switch(current) {
						case '"':		
							accumulator.append('"');
							state = State.OPEN;					
							break;						
							
						// case ',':				
						default:
							accumulator.append((char) current);
							state = State.QUOTED;
						}
						if (! started) {
							termStartSpot = blockCursor;
							started = true;
						}
						break;
						
					case QUOTED:	
						raw.append((char) current);
						switch(current) {
						case '"':							
							state = State.QUOTE_IN_QUOTE;
							break;
							
						// case ',':					
						default:
							accumulator.append((char) current);
							break;						
						}
						break;				
					
					case QUOTE_IN_QUOTE:				
						switch(current) {
						case '"':
							raw.append((char) current);
							accumulator.append('"');
							state = State.QUOTED;
							break;						
							
						case ',':
							raw.append((char) current);
							terms.add(new Term(accumulator.toString(), termStartSpot, blockCursor - 1));
							accumulator = new StringBuilder();
							state = State.OPEN;
							started = false;
							break;
							
						case '\n':
						case '\r':
							terms.add(new Term(accumulator.toString(), termStartSpot, blockCursor - 1));
							accumulator = new StringBuilder();
							state = State.DONE;
							current = -1;
							break;
							
						default:
							raw.append((char) current);
							accumulator.append((char) current);
							state = State.OPEN;
							break;													
						}
						break;
						
					case DONE:
						break;
						
					}
					
				}
				
				// dangles
				if (state.ordinal() > State.OPEN.ordinal()) {
					throw new AnnotatedException("Dangling CSV field.").annotate("location", sourceSpot);
					
				} else if (accumulator.length() > 0) {
					terms.add(new Term(accumulator.toString(), termStartSpot, blockCursor));					
				} 
				
				if (current < 0) break;

			} // end while
			
			if (terms.size() > 0) {			
				result = new Block(new BlockInfo(BlockType.LINE, ownerId, blockId), raw.toString(),
						terms.toArray(new Term[terms.size()])); 
				result.info.bounded(sourceBlockMark, sourceSpot);
			}
			
		} catch (Exception e) {
			throw new AnnotatedException("Failed to read block.", AnnotatedException.Catagory.FAULT, e)
				.annotate("location", sourceSpot);
		}
		
		return result;
	}
	
}
