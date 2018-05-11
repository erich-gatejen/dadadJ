package dadad.platform;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Tabular text format encoder/decoder.
 * <p>
 * Rules:
 * <code>
 *    - Tabs are encoded as \t
 *    - Line breaks are encoded as \r and \n.  Generally elements should avoid these anyway.
 *    - Backslashes are encoded as \\
 * </code>
 * 
 */
public class TabularEncoding {
	
	// ===============================================================================
	// = FIELDS

	public final static int ESCAPE_PAD = 16;
	
	private final static Pattern splitter = Pattern.compile("\\t");
	
	// ===============================================================================
	// = METHODS
	
	/**
	 * Decode a string.
	 * @param source
	 * @return the elements or null if an empty (whitespace only) string.
	 */
	public static String[] decode(final String source) {
		if (source.trim().length() < 1) return null;
		String[] result = splitter.split(source);
		for (int index = 0; index < result.length; index++) {
			result[index] = deescape(result[index]);
		}
		return result;
	}
	
	public static String encode(final Object... source) {
		return encode(Arrays.asList(source));
	}
	
	public static String encode(final List<?> source) {
		StringBuilder sb = new StringBuilder();
				
		if (source.size() > 0) {
			
			Iterator<?> iter = source.iterator();
			sb.append(escape(iter.next()));
			
			while(iter.hasNext()) {
				sb.append('\t');
				sb.append(escape(iter.next()));
			}
		}
		
		return sb.toString();
	}	
	
	public static String escape(final Object obj) {
		String source = obj.toString();
		
		StringBuilder sb = new StringBuilder(source.length() + ESCAPE_PAD);
		final CharacterIterator it = new StringCharacterIterator(source);
		for(char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
			switch (c) {
			case '\n':
				sb.append("\\n");
				break;
				
			case '\r':
				sb.append("\\r");
				break;
			
			case '\t':
				sb.append("\\t");
				break;
			
			case '\\':
				sb.append("\\\\");
				break;
				
			default:
				sb.append(c);
				break;
			}
		}
		
		return sb.toString();
	}
	
	public static String deescape(final String source) {
		if (source.length() < 1) return source;
		
		boolean open = false;
		StringBuilder sb = new StringBuilder(source.length());
		for (int index = 0; index < source.length(); index++) {
			if (open) {
				switch(source.charAt(index)) {
				case 'n':
					sb.append('\n');
					break;
					
				case 'r':
					sb.append('\r');
					break;
					
				case 't':
					sb.append('\t');
					break;
					
				case '\\':
					sb.append('\\');
					break;
								
				default:
					throw new AnnotatedException("Improper escape in encoded string.")
						.annotate("character.position", index, "encoded.string", source);	
				}				
				open = false;
				
			} else {
				if (source.charAt(index) == '\\') {
					open = true;
				} else {
					sb.append(source.charAt(index));
				}
			}
		}
		
		if (open) throw new AnnotatedException("Dangling escape at end of encoded string.")
				.annotate("encoded.string", source);	
		
		return sb.toString();	
	}

}
