package dadad.data.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import dadad.data.model.*;
import dadad.data.model.Document;
import dadad.platform.AnnotatedException;
import dadad.platform.config.Configuration;

/**
 * A sleezy implementation.
 * 
 * A huge optimization would be to make sure the term strings were just slices of the obj/block strings.\
 *
 */
public class JSONBlockReader extends BlockReader {
	
	// ===============================================================================
	// = FIELDS
	
	public final static int JSON_OBJECT_OPEN = '{';
	public final static int JSON_OBJECT_CLOSE = '}';
	public final static int JSON_ARRAY_OPEN = '[';
	public final static int JSON_ARRAY_CLOSE = ']';	
	public final static int JSON_NAME_DELIMIT = '"';	
	public final static int JSON_TERM_DELIMIT = ':';
	public final static int JSON_ESCAPE = '\\';

	private Queue<Block> blockQueue;
	private BufferedReader brin;
	
	private long sourceSpot;			// Current spot in the source
	private long sourceBlockMark;		// Mark in the source the start of the block
	
	// Term offsets
	private long termStartSpot;			// Start within the block where a term starts
	private long blockCursor;			// Current cursor position within a block

	private long blockId;
	private long ownerId;

	
	// ===============================================================================
	// = METHODS
	
	/**
	 * Constructor.  IMPORTANT: this will not close the reader!
	 * @param inputReader
	 */
	public JSONBlockReader(final Reader inputReader, final Document source, final Configuration config) {
		super(inputReader, source, config);
		
		blockQueue = new LinkedBlockingQueue<Block>();
		brin = new BufferedReader(getReader());
		sourceSpot = 0;
		
	}
	
	// ===============================================================================
	// = INTERFACE
	
	protected Block readBlock(final long blockId, final long ownerId) {
		Block result = null;
		this.blockId = blockId;
		this.ownerId = ownerId;

		if (blockQueue.size() < 1 ) {
						
			blockCursor = -1;
			sourceBlockMark = sourceSpot;
			
			StringBuilder buffer = new StringBuilder();	
			try {
	
				int currentChar = readChar();
				while (currentChar >= 0) {
					
					
					if (currentChar == JSON_OBJECT_OPEN) {						
						if (buffer.length() > 0) {
							blockQueue.add(block(BlockType.INTERSPACE, buffer.toString(), null));
							blockCursor = 0;
						}
												
						objAccum = new StringBuilder();	
						objAccum.append((char) currentChar);	
						buffer = new StringBuilder();
						
						readObject();
						break;
						
					} else {
						buffer.append((char) currentChar);	
						
					}
					
					currentChar = readChar();
				}
				
				if (buffer.length() > 0) {
					blockQueue.add(block(BlockType.INTERSPACE, buffer.toString(), null));
				}
				
			} catch (Exception e) {
				throw new AnnotatedException("Failed to read block.", AnnotatedException.Catagory.FAULT, e)
					.annotate("location.in.source", sourceSpot);
			}
			
			if ((result == null) && (blockQueue.size() > 0)) result = blockQueue.remove();
		
		} else {			
			result = blockQueue.remove();
			
		}
		
		return result;
	}
	
	// ===============================================================================
	// = INTERNAL

	/*
		{"menu": {
		  "id": "file",
		  "value": "File",
		  "popup": {
		    "menuitem": [
		      {"value": "New", "onclick": "CreateNewDoc()"},
		      {"value": "Open", "onclick": "OpenDoc()"},
		      {"value": "Close", "onclick": "CloseDoc()"}
		    ]
		  }
		}}
	 */

	private ArrayList<Term> terms;
	private StringBuilder objAccum;
	private StringBuilder termAccum;
	private int objectPly;		
	
	private int readChar() throws IOException {
		blockCursor++;
		sourceSpot++;
		return brin.read();
	}
	
	private int readObjectChar() throws IOException {
		int result = readChar();
		objAccum.append((char) result);
		return result;
	}
	
	private int readTermChar() throws IOException {
		int result = readObjectChar();
		termAccum.append((char) result);
		return result;
	}
	
	private void readObject() throws IOException {

		objectPly = 0;
		terms = new ArrayList<Term>();
		
		int currentChar = readObjectChar();				
		while (true) {
			if (currentChar < 0) {
				throw new RuntimeException("Incomplete JSON object.  Stream truncated.");
			}
			
			switch (currentChar) {
			
			case JSON_OBJECT_OPEN:
				objectPly++;
				break;
			
			case JSON_OBJECT_CLOSE:
				if (objectPly == 0) {
					blockQueue.add(block(BlockType.SCOPE, objAccum.toString(), terms.toArray(new Term[terms.size()])));
					return;
					
				} else {
					objectPly--;
					if (objectPly < 0) throw new RuntimeException("Unbalanced object definitions  (More '}' closes than '{' opens).");
				}				
				break;
				
			case JSON_NAME_DELIMIT:
				termAccum = new StringBuilder();
				termAccum.append((char) currentChar);
				termStartSpot = blockCursor;
				readName();
				break;
				
			case '\r':
			case '\n':
			case ' ':
			case '\t':
			case '\f':
			case ',':
			case JSON_ARRAY_CLOSE:
				break;
				
			default:
				throw new RuntimeException("Interspace character not allowed.  char='" + (char) currentChar + "'");
			}
			
			currentChar = readObjectChar();
		}
		
	}
	
	private void readName() throws IOException {

		int currentChar = readTermChar();	
		
		while (true) {
			if (currentChar < 0) {
				throw new RuntimeException("Incomplete JSON object.  Stream truncated while reading name.");
			}
			
			if (currentChar == JSON_ESCAPE) {
				if (readTermChar() < 0) throw new RuntimeException("Stream truncated leaving dangling escape while reading name.");
				
			} else if (currentChar == JSON_NAME_DELIMIT) {
				readDelimit();
				return;
			}			
			currentChar = readTermChar();
		}		
	}
	
	
	private void readDelimit() throws IOException {
		int currentChar = readTermChar();				
		while (true) {
			if (currentChar < 0) {
				throw new RuntimeException("Incomplete JSON object.  Stream truncated while seeking term delimiter.");
			}
			switch (currentChar) {
				
			case JSON_TERM_DELIMIT:
				readValueLeader();
				return;
				
			case '\r':
			case '\n':
			case ' ':
			case '\t':
			case '\f':
				break;
				
			default:
				throw new RuntimeException("Term character not allowed.  Was seeking term delimiter (:).  char='" + (char) currentChar + "'");
			}							
			currentChar = readTermChar();
		}		
	}
	
	private void readValueLeader() throws IOException {
		int currentChar = readTermChar();				
		while (true) {
			if (currentChar < 0) {
				throw new RuntimeException("Incomplete JSON object.  Stream truncated while seeking term.");
			}
			switch (currentChar) {
				
			case JSON_NAME_DELIMIT:
				readValue();
				return;
				
			case JSON_OBJECT_OPEN:
				// embedded object.
				objectPly++;
				return;
				
			case JSON_ARRAY_OPEN:
				return;
				
			case '\r':
			case '\n':
			case ' ':
			case '\t':
			case '\f':
				break;
				
			case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
				readNumber((char) currentChar);
				return;
				
			default:
				throw new RuntimeException("Term character not allowed.  Was seeking term value.  char='" + (char) currentChar + "'");
			}							
			currentChar = readTermChar();
		}		
	}
	
	
	private void readValue() throws IOException {

		int currentChar = readTermChar();				
		while (true) {
			if (currentChar < 0) {
				throw new RuntimeException("Incomplete JSON object.  Stream truncated while accumulating value.");
			}
			switch (currentChar) {
				
			case JSON_NAME_DELIMIT:
				terms.add(new Term(termAccum.toString(), termStartSpot, blockCursor + 1));
				return;
				
			case JSON_ESCAPE:
				if (readTermChar() < 0) throw new RuntimeException("Incomplete JSON object.  Stream truncated while escaping value.");
				break;
			
			default:
				break;
			}							
			currentChar = readTermChar();
		}		
	}
	
	private void readNumber(final char entryChar) throws IOException {
		boolean seenPeriod = false;
		boolean seenE = false;
		
		int currentChar = queryNumberChar();
		if ((currentChar == '-') || (currentChar == '+')) {
			acceptNumberTermChar(currentChar);
			currentChar = queryNumberChar();
		}
		
		outer:
		while (true) {
			if (currentChar < 0) {
				throw new RuntimeException("Incomplete JSON object.  Stream truncated whilereading number.");
			}

			if (currentChar == JSON_OBJECT_CLOSE) {
				rejectNumberTermChar();
				terms.add(new Term(termAccum.toString(), termStartSpot, blockCursor + 1));
				return;
			} 
			
			acceptNumberTermChar(currentChar);
			switch(currentChar) {
			case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
				break;
				
			case '.':
				if (seenPeriod)
					throw new RuntimeException("Encountered second period in numeric.");
				else 
					seenPeriod = true;
				break;
			
			case 'E':
			case 'e':
				if (seenE)
					throw new RuntimeException("Encountered second E in numeric.");
				else 
					seenE = true;
					currentChar = queryNumberChar();
					if ((currentChar == '-') || (currentChar == '+')) {
						acceptNumberTermChar(currentChar);
						currentChar = queryNumberChar();
					}
				continue outer;

			default:
				terms.add(new Term(termAccum.toString(), termStartSpot, blockCursor + 1));
				return;
				
			}							
			currentChar = queryNumberChar();
		}		
	}
	
	private Block block(final BlockType type, final String text, Term[] terms) {
		if (terms == null) {
			terms = new Term[1];
			terms[0] = new Term(text, 0, text.length());		
		}
	
		Block block = new Block(new BlockInfo(type, ownerId, blockId), text, terms);
		block.info.bounded(sourceBlockMark, sourceSpot);
		return block;
		
	}
	
	// HAX for reading numbers, since they can be terminated by a object close char
	
	private int queryNumberChar() throws IOException {
		brin.mark(2);
		return brin.read();
	}
	
	private void acceptNumberTermChar(final int chararacter) {
		blockCursor++;
		sourceSpot++;
		objAccum.append((char) chararacter);
		termAccum.append((char) chararacter);
	}
	
	private void rejectNumberTermChar()  throws IOException  {
		brin.reset();
	}
	
	
}
