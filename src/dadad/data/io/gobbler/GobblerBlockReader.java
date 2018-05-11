package dadad.data.io.gobbler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import dadad.data.config.GobblerConfiguration;
import dadad.data.io.BlockReader;
import dadad.data.model.Block;
import dadad.data.model.BlockInfo;
import dadad.data.model.BlockType;
import dadad.data.model.Document;
import dadad.data.model.Term;
import dadad.platform.AnnotatedException;
import dadad.platform.config.Configuration;

import static dadad.platform.Constants.ANNOTATION__LINE_NUMBER;

public class GobblerBlockReader extends BlockReader {
	
	// ===============================================================================
	// = FIELDS

	private long lineNumber;
	private BufferedReader brin;
	
	private GobblerProgram gobbler;
	
	// ===============================================================================
	// = METHODS
	
	/**
	 * Constructor.  IMPORTANT: this will not close the reader!
	 * @param inputReader
	 */
	public GobblerBlockReader(final Reader inputReader, final Document source, final Configuration config) {
		super(inputReader, source, config);
		
		String programPath = config.getRequired(GobblerConfiguration.PROGRAM_FILE);
		brin = new BufferedReader(getReader());
		lineNumber = 0;
		
		Reader programReader = null;
		try {
			try {
				programReader = new BufferedReader(new FileReader(programPath));
				gobbler = new GobblerProgram("gobble", programReader);
			
			} catch (IOException ioe) {
				throw new AnnotatedException("Could not open gobbler program file.", AnnotatedException.Catagory.FAULT, ioe);
			}
		} catch (AnnotatedException e) {	
			throw e.annotate(GobblerConfiguration.PROGRAM_FILE.property(), programPath);
			
		} finally {
			try {
				programReader.close();
			} catch (Exception e) {
				// NOP
			}
		}
	}
		
	
	// ===============================================================================
	// = INTERFACE

	List<Term> gobbled = new ArrayList<Term>();
		
	protected Block readBlock(final long blockId, final long ownerId)  {
	
		Block result = null;
		try {
			String line;
			do {
			    line = brin.readLine();
				if (line == null) return null;		// Empty reader, so we are done.
				gobbled = gobbler.execute(line);
			} while (gobbled.isEmpty());

			result = new Block(new BlockInfo(BlockType.LINE, ownerId, blockId), line, gobbled.toArray(new Term[gobbled.size()]));
			result.info.bounded(0, line.length() - 1);

						
		} catch (Exception e) {
			throw new AnnotatedException("Failed to read block.", AnnotatedException.Catagory.FAULT, e)
				.annotate(ANNOTATION__LINE_NUMBER, lineNumber);
		}
		
		return result;
	}
	
}
