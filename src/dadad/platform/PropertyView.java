package dadad.platform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public interface PropertyView {
	
	public final static char PROPERTY_PATH_SEPARATOR = '.';
	
	public final static char PROPERTY_MULTIVALUE_SEPARATOR = ',';
	public final static String PROPERTY_MULTIVALUE_SEPARATOR__STRING = ",";
	public final static String PROPERTY_MULTIVALUE_SEPARATOR__ESCAPED = "\\,";
	public final static char PROPERTY_MULTIVALUE_ESCAPE = '\\';
	public final static String PROPERTY_MULTIVALUE_ESCAPE__STRING = "\\";
	public final static String PROPERTY_MULTIVALUE_ESCAPE__ESCAPED = "\\\\";
	
	
	public final static String PROPERTY_EMPTY = "";	
	public final static char PROPERTY_TEXT_COMMENT = '#';
	public final static char PROPERTY_TEXT_FOLD = '/';
	public final static char PROPERTY_TEXT_FOLD_BREAK = '~';	
	public final static char PROPERTY_TEXT_EQUALITY = '=';	
	
	public String path(final String... path);
	
	public boolean exists(final String... path);
	
	public String get(final String path);
	public String get(final String... path);
	public String[] getMultivalue(final String path);
	public String[] getMultivalue(final String... path);
	public void set(final String path, final String value);
	public void set(final String path, final String value, final boolean decode);
	public void set(final String path, final String... value);
	public void append(final String path, final String value);
	public void append(final String path, final String value, final boolean decode);
	public void append(final String path, final String... value);
	
	public void remove(final String path);
	public void remove(final String... path);
	
	public void prune(final String path);
	public void prune(final String... path);
	
	public void graft(PropertyView sourceView);
	
	public PropertyView branch(final String path);
	public PropertyView branch(final String... path);

	public Collection<String> sub(final String path);
	public Collection<String> sub(final String... path);
	public Collection<String> ply(final String path);
	public Collection<String> ply(final String... path);
	
	public void save();
	public void save(final BufferedWriter bw);
	
	public PropertyStore load(final File propFile);
	public PropertyStore load(final Reader reader, final String fileNAme);
	
	public PropertyView copy(final String path);
	public PropertyView copy(final String... path);
	
	public Map<String, String[]> getAll();
	
	public PropertyStore shadow();
	
	public ResolverHandler getResolveHandler();
	
	public static String encode(final String... values) {
		StringBuilder sb = new StringBuilder();
		if (values.length > 0) sb.append(escapeEncode(values[0]));
		for (int index = 1; index < values.length ; index++) {
			sb.append(PROPERTY_MULTIVALUE_SEPARATOR);
			sb.append(escapeEncode(values[index]));
		}
		return sb.toString();
	}
	
	public static String escapeEncode(final String value) {
		return value.replace(PROPERTY_MULTIVALUE_ESCAPE__STRING, PROPERTY_MULTIVALUE_ESCAPE__ESCAPED)
				.replace(PROPERTY_MULTIVALUE_SEPARATOR__STRING, PROPERTY_MULTIVALUE_SEPARATOR__ESCAPED);
	}
	
	public static String[] decode(final String value) {
		StringBuilder accumulator = new StringBuilder();
		ArrayList<String> items = new ArrayList<String>();
		boolean escaping = false;
		char current;
		
		for (int index = 0; index < value.length(); index++) {
			current = value.charAt(index);
			if (escaping) {
				accumulator.append(current);
				escaping = false;
				
			} else {				
				if (current == PROPERTY_MULTIVALUE_SEPARATOR) {
					if (accumulator.length() > 0) {
						items.add(accumulator.toString());
						accumulator = new StringBuilder();
					}
				} else if (current == PROPERTY_MULTIVALUE_ESCAPE) {
					escaping = true;
				} else {
					accumulator.append(current);
				}			
			}			
		}
		
		// dangles
		if (escaping == true) {
			throw new AnnotatedException("Dangling escape for multivalue.").annotate("value", value);			
		} else if (accumulator.length() > 0) {
			items.add(accumulator.toString());
		}

		String[] itemsArray = new String[items.size()];
		return items.toArray(itemsArray);
	}
	
}
