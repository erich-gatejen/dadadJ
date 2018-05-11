package dadad.data.io;

import java.io.Writer;

import dadad.data.model.Block;
import dadad.platform.AnnotatedException;
import dadad.platform.config.Configuration;

import static dadad.platform.Constants.ANNOTATION__LINE_NUMBER;

/**
 * Writes the raw block data to a file line by line.
 *
 */
public class LineBlockWriter extends BlockWriter {
	
	// ===============================================================================
	// = FIELDS

	private long lineNumber;
	
	// ===============================================================================
	// = METHODS
	
	/**
	 * Constructor.  IMPORTANT: this will not close the writer!
	 * @param inputReader
	 */
	public LineBlockWriter(final Writer outputWriter, final Configuration config) {
		super(outputWriter, config);
		lineNumber = 0;
	}
	
	// ===============================================================================
	// = INTERFACE

	public void write(final Block block) {
	
		try {

			if (block != null) {
				writer.write(block.raw);
				writer.newLine();
			}
						
		} catch (Exception e) {
			throw new AnnotatedException("Failed to write block.", AnnotatedException.Catagory.FAULT, e)
				.annotate(ANNOTATION__LINE_NUMBER, lineNumber);
		}
		
	}
	
}
