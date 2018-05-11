package dadad.process.data.test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;

import dadad.data.model.Block;
import dadad.data.model.Term;
import dadad.platform.Context;
import dadad.platform.test.BlockTest;

public class JSONTermsTest extends BlockTest {

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
		return "json.terms";
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
			int currentChar = srin.read();
			while(currentChar >= 0) {
				
				if (currentChar == '"') {
					String slurpString = termSlurpString(srin);
					if (slurpString.length() > 0) foundTerms.add(slurpString);
					
				} else if (currentChar == '\\') {
					currentChar = srin.read();
					
				} else if (Character.isLetterOrDigit(currentChar)) {
					String slurpAN = termSlurpAlphaNumeric(srin, (char) currentChar);
					if (slurpAN.length() > 0) foundTerms.add(slurpAN);
				}			
				
				currentChar = srin.read();
			}
		
		} catch (IOException ioe) {
			
			
			
			
			throw new Error("BUG BUG BUG!   This should never happen.  StringReader shouldn't throw exceptions.");			
		}
		
		for (Term term : target.getTerms()) {			
			if (! foundTerms.contains(term.text)) {
				
				
				fail("Term not found in text.  value=[" + term.text + "]");
			}
		}
	}
	
	private String termSlurpAlphaNumeric(final Reader rin, char initialCharacter) throws IOException {
		StringBuilder accum = new StringBuilder();
		accum.append(initialCharacter);
		
		int currentChar = rin.read();
		while(currentChar >= 0) {
			
			if (Character.isLetterOrDigit(currentChar)) {
				accum.append((char) currentChar);
				
			} else if (currentChar == '\\') {
				currentChar = rin.read();
				if (currentChar < 0) throw new RuntimeException("Dangling escape.");
				accum.append((char) currentChar);
				
			} else {
				break;
				
			}			
			
			currentChar = rin.read();
		}	
		
		return accum.toString();
	}
	
	private String termSlurpString(final Reader rin) throws IOException {
		StringBuilder accum = new StringBuilder();
		
		int currentChar = rin.read();
		while(currentChar >= 0) {
			
			if (currentChar == '"') {
				break;
				
			} else if (currentChar == '\\') {
				currentChar = rin.read();
				if (currentChar < 0) throw new RuntimeException("Dangling escape.");
				accum.append((char) currentChar);
				
			} else {
				accum.append((char) currentChar);
				
			}			
			
			currentChar = rin.read();
		}	
		
		return accum.toString();
	}
}

