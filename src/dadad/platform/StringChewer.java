package dadad.platform;

/**
 * Chew on a string.
 */
public class StringChewer {

	// ===============================================================================
	// = FIELDS
	
	private final String string;
	private int spot;

	// ===============================================================================
	// = METHOD
	
	public StringChewer(final String string) {
		this.string = string;	
		spot = 0;
	} 
	
	public boolean hasMore() {
		if (spot < string.length()) return true;
		return false;
	}
	
	public char	next() {
		char result = string.charAt(spot);
		spot++;
		return result;
	}
	    
}

