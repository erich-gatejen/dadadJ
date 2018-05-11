package dadad.process.data.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;

import dadad.data.model.Block;
import dadad.data.model.Term;
import dadad.platform.Context;
import dadad.platform.test.BlockTest;

public class CSVTermsTest extends BlockTest {

	// ===============================================================================
	// = FIELDS
	
	
	// ===============================================================================
	// = ABSTRACT
	
	/**
	 * Get the name of the test.  Do not use NAMESPACE_SEPARATOR in the name.  The name must be fixed 
	 * at the time _run is called, so you may as well make it a static.
	 * @return the name
	 */
	public String name() {
		return "csv.terms";
	}
	
	/**
	 * Run the test.
	 * @param context test context.
	 * @param target test target.
	 */
	public void _run(final Context context, final Block target) {	
		termClip(target);			
	}
		

	// ===============================================================================
	// = METHODS
	
	private void termClip(final Block target) {
		
		HashSet<String> foundTerms = new HashSet<String>();	
		try {
			StringReader srin = new StringReader(target.raw);
			StringBuilder accum = new StringBuilder();
			
			int currentChar = srin.read();	
			while(currentChar >= 0) {
								
				switch(currentChar) {
				
				case '"':
					currentChar = srin.read();
					if (currentChar == '"') {
						accum.append('"');
					} else {
						int nextChar = quote(srin, accum, currentChar);
						if (nextChar >= 0) {
							currentChar = nextChar;
							continue;
						}
					}						
					break;
					
				case ',':
					String term = accum.toString().trim();
					if (term.length() > 0) foundTerms.add(term);
					accum = new StringBuilder();
					break;
					
				default:
					accum.append((char) currentChar);
					break;			
				}		
				
				currentChar = srin.read();
			}
			
			String term = accum.toString().trim();
			if (term.length() > 0) foundTerms.add(term);			
		
		} catch (IOException ioe) {	
			throw new Error("BUG BUG BUG!   This should never happen.  StringReader shouldn't throw exceptions.");			
		}
		
		for (Term term : target.getTerms()) {			
			if ((term.text != null) && (term.text.trim().length() > 1) && (! foundTerms.contains(term.text))) {				
				fail("Term not found in text.  value=[" + term.text + "]");
			}
		}
	}
	
	private int quote(final StringReader srin, final StringBuilder accum, int inboundCurrentChar) throws IOException {

		int currentChar = inboundCurrentChar;
		while(true) {
			
			if (currentChar < 0) {
				throw new RuntimeException("Dangling quote in CSV");
				
			} else if (currentChar == '"') {
							
				int nextChar = srin.read();
				if (nextChar < 0) return -1;
				if (nextChar == '"') {
					accum.append('"');				
				} else {
					return nextChar;				
				}
			
			} else {
				accum.append((char) currentChar);	
			}		
			
			currentChar = srin.read();
		}
	}
	
}

