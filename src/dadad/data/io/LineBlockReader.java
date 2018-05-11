package dadad.data.io;

import java.io.BufferedReader;
import java.io.Reader;

import dadad.data.model.*;
import dadad.data.model.Document;
import dadad.platform.AnnotatedException;
import dadad.platform.config.Configuration;

import static dadad.platform.Constants.ANNOTATION__LINE_NUMBER;

public class LineBlockReader extends BlockReader {
	
	// ===============================================================================
	// = FIELDS

	private long lineNumber;
	private long start;
	private BufferedReader brin;
	
	// ===============================================================================
	// = METHODS
	
	/**
	 * Constructor.  IMPORTANT: this will not close the reader!
	 * @param inputReader a reader
	 * @param document owning document
     * @param config current configuration.
	 */
	public LineBlockReader(final Reader inputReader, final Document document, final Configuration config) {
		super(inputReader, document, config);
		brin = new BufferedReader(inputReader);
		lineNumber = 0;
		start = 0;
	}
	
	// ===============================================================================
	// = INTERFACE

	protected Block readBlock(final long blockId, final long ownerId) {
	
		Block result = null;
		try {

			String line = brin.readLine();
			if (line != null) {
				Term[] terms = new Term[1];
				terms[0] = new Term(line, 0, line.length());
				
				result = new Block(new BlockInfo(BlockType.LINE, ownerId, blockId), line, terms);
				result.info.bounded(start, start + (line.length() - 1));
				start = start + line.length();	
			}
						
		} catch (Exception e) {
			throw new AnnotatedException("Failed to read block.", AnnotatedException.Catagory.FAULT, e)
				.annotate(ANNOTATION__LINE_NUMBER, lineNumber);
		}
		
		return result;
	}
	
}
