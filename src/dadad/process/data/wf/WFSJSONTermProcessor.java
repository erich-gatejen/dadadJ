package dadad.process.data.wf;

import dadad.data.DataContext;
import dadad.data.config.CSVConfiguration;
import dadad.data.config.FieldConfiguration;
import dadad.data.config.WorkflowConfiguration;
import dadad.data.model.Block;
import dadad.data.model.ElementType;
import dadad.data.model.Term;
import dadad.platform.config.ConfigurationType;
import dadad.process.WorkflowStep;

import java.io.IOException;
import java.io.StringReader;

public class WFSJSONTermProcessor extends WorkflowStep<Block, DataContext> {

	// ===============================================================================
	// = FIELDS
	
	
	// ===============================================================================
	// = INTERFACE
	
	public boolean _takesInterspace() {
		return false;
	}
	
	// ===============================================================================
	// = ABSTRACT
	
	private static Class<?>[] configs = new Class<?>[]{   WorkflowConfiguration.class, CSVConfiguration.class, FieldConfiguration.class };
	@SuppressWarnings("unchecked")
	public Class<ConfigurationType>[] getUsedConfigurations() {
		return (Class<ConfigurationType>[]) configs;
		
	}
	
	protected ConfigurationType[] _required() {
		return null;
	}
	
	protected void _start() {
		// NOP
	}
	
	protected Block _step(Block block) {
		Block result = block;
		
		Term[] terms = block.getTerms();
		Term[] newTerms = new Term[terms.length];
		if (terms != null) {

			for (int index = 0; index < terms.length; index++) {
				newTerms[index] = process(terms[index]);
			}
			
			block.alterTerms(newTerms);			
		}
		
		return result;
	}

	public void _end() {
		// NOP
	}
	
	public void _close() {
		// NOP
	}
	
	// ===============================================================================
	// = METHODS
	
	private StringReader reader;
	private int termCursor;
	private int valueStartInTerm;

	/**
	 * "name"(SPACE):(SPACE)"value"
	 * "name"(SPACE):(SPACE)0123456789*(?)
	 *  
	 * 
	 * @param term
	 * @return
	 */
	private Term process(final Term term) {	
		termCursor = 0;
		reader = new StringReader(term.text);

		try {
		
			// Get name
			is(notNull(read(), "Looking for name") , '"', "Expecting double quote to open name");
			while((notNull(read(), "Reading name") != '"'));
			String name = term.text.substring(1, termCursor - 1);
			
			while((notNull(read(), "Seeking n/v separator") != ':'));
			
			int character = notNull(read(), "Seeking value");
			while(true) {
				switch(character) {			
					
				case '\r':
				case '\n':
				case ' ':
				case '\t':
				case '\f':
					break;
					
				case '"':
					valueStartInTerm = termCursor;
					return getValue(name, term);
					
				case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
					valueStartInTerm = termCursor - 1;
					return getNumeric(name, term);
					
				default:
					throw new RuntimeException("Value must start with a double quote or a numeric.  char='" + (char) character + "'");			
				
				}
				character = notNull(read(), "Seeking value");
			} 
		
		} catch (IOException ioe) {
			throw new Error("This should be impossible, since we are reading from a String.", ioe);
		}

	}
	
	private Term getValue(final String name, final Term term) throws IOException {
		
		int character = notNull(read(), "Reading value");
		while(true) {
			switch(character) {			
				
			case '\r':
			case '\n':
			case ' ':
			case '\t':
			case '\f':
				break;
				
			case '\\':
				notNull(read(), "Value truncated while escaped.");
				break;
				
			case '"':
				return newTerm(term, name, valueStartInTerm, termCursor);
				
			default:
				break;			
			
			}
			character = notNull(read(), "Value truncated withtout closing with double quote.");
		} 
				
	}
	
	private Term getNumeric(final String name, final Term term) throws IOException {

		int character = notNull(read(), "Reading value");
		while(true) {
			switch(character) {		
			
								
			case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
				// Should probably do more to protect these, but we will be very forgiving.
			case '.': case 'E': case '+': case '-':
				break;	
				
			case '\r':
			case '\n':
			case ' ':
			case '\t':
			case '\f':
			case ',':
				return newTerm(term, name, valueStartInTerm, termCursor);	
				
			default:
				throw new RuntimeException("Character not allowed in a numeric.  char='" + (char) character + "'");			
				
			}
			character = notNull(read(), "Value truncated withtout closing with double quote.");
		} 
				
	}

	private Term newTerm(final Term term, final String name, final int offsetStart, final int runEnd) {
		Term result = new Term(term.text.substring(offsetStart, runEnd - 1), term.start + offsetStart, (term.start + runEnd) - 1);
		result.set(ElementType.UNKNOWN, name, result.text);
		return result;
	}
	
	private int read() throws IOException {
		termCursor++;
		return reader.read();
	}
	
	private int notNull(final int character, final String error) {
		if (character < 0) throw new RuntimeException("Stream truncated.  " + error);
		return character;
	}
	private int is(final int character, final int is,  final String error) {
		if (character != is) throw new RuntimeException("Unexpected character.  " + error);
		return character;
	}
	
}
