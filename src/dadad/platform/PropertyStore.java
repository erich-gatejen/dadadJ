package dadad.platform;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

/**
 * Property store.
 * 
 * All paths are normalized to trimmed and lower case.  Whitespace is allowed within elements, but should not be
 * used as bookends to the path elements or it can be lost.
 * 
 * The original D version of this used actual trees.  This one is mostly adapted from THINGS.  Performance just isn't
 * really a problem.
 *
 */
public class PropertyStore implements PropertyView, ResolverHandler {

	// ===============================================================================
	// = FIELDS
	
	private HashMap<String, String[]> properties = null;
	private LinkedList<HashMap<String, String[]>> shadows;
	private String root = null;
	
	private Stack<String> source;  // Protected by sync
	//private String mostRecentFile;
	
	// ===============================================================================
	// = METHODS
    
	public PropertyStore() {
		initialize();
	}
	
	public PropertyStore(HashMap<String, String[]> properties, final String root) {
		this.root = root;
		this.properties = properties;	
		source = new Stack<String>();
		shadows = new LinkedList<HashMap<String, String[]>>();
		
	}
	
	private PropertyStore(final String root, final Stack<String> source,
			final LinkedList<HashMap<String, String[]>> shadows, final HashMap<String, String[]> properties) {
		this.root = root;
		this.source = source;
		this.shadows = shadows;
		this.properties = properties;		
	}
	
	private void initialize() {
		root = "";
		properties = new HashMap<String, String[]>();	
		source = new Stack<String>();
		shadows = new LinkedList<HashMap<String, String[]>>();
	}
	
	public String path(final String... path) {
		if ((path == null) || (path.length < 1)) return PROPERTY_EMPTY;		
		StringBuilder result = new StringBuilder();
		if ((path[0] != null) && (path[0].trim().length() > 0)) result.append(path[0].trim());
		for (int index = 1; index < path.length; index++) {
			if ((path[index] != null) && (path[index].trim().length() > 0)) {
				result.append(PROPERTY_PATH_SEPARATOR).append(path[index].trim());
			}
		}
		return result.toString();				
	}
	
	public String normalizePath(final String path) {
		if (path == null) throw new Error("BUG: path cannoth be null");
		return path.trim().toLowerCase();
	}
	
	private String fixPath(final String path) {
		if (root.length() < 1)
			return normalizePath(path);			
		else
			return root + PROPERTY_PATH_SEPARATOR + normalizePath(path);
	}
	
	private String fixPathNotEmpty(final String path) {
		String result = fixPath(path);
		if (result.length() < 1) throw new Error("BUG: path cannoth be empty for getter or setter.");
		return result;
	}
	
	public synchronized PropertyStore load(final File propFile) {
		newSource(propFile.getAbsolutePath());
		return _load(propFile);
	}
	
	public synchronized PropertyStore load(final Reader reader, final String fileName) {
		newSource(fileName);		
		return _load(reader);
	}
	
	private void newSource(final String sourceName) {
		if (source.size() == 1) {
			source.pop();
		} if (source.size() > 1) {
			throw new Error("BUG BUG BUG!  This was called through the include logic somehow.");
		}
		source.push(sourceName);
	}
	
	private PropertyStore _load(final File propFile) {
		FileReader fr;
				
		try {
			fr = new FileReader(propFile);
			
		} catch (Exception e) {
			throw new AnnotatedException("Could not open property file for reading.", AnnotatedException.Catagory.FAULT, e)
				.annotate("file", propFile.getAbsolutePath());
		}
		
		try {
			_load(fr);
			
		} finally {
			try {
				fr.close();
			} catch (Throwable t) {
				// Don't care
			}
		}
		return this;
	}
	
	public PropertyStore _load(final Reader reader) {
		
		ExceptionBundleException exceptionBundleException = new ExceptionBundleException("Failed to load properties due to errors.");
		StringBuilder buffer = new StringBuilder();
		
		BufferedReader bir = null;
		if (bir instanceof BufferedReader) {
			bir = (BufferedReader) reader;
		} else {
			bir = new BufferedReader(reader);
		}
			
		Resolver resolver = new Resolver(this);
				       
    	boolean continuing = false;
    	int lineNumber = 1;
    	
    	try {
	        String currentLine = bir.readLine();		// Assumes PROPERTY_LINE_TERMINATION = \r\n
	        while(currentLine != null) {
	        	currentLine = currentLine.trim();
	        	
	            if (currentLine.length() > 0) {
	            	
		            if	((currentLine.charAt(0) != PROPERTY_TEXT_COMMENT) || continuing) {
		            	
		                if (currentLine.charAt(currentLine.length() - 1) == PROPERTY_TEXT_FOLD) {
	                        buffer.append(currentLine.substring(0, currentLine.length() - 1));	                    
		                } else if (currentLine.charAt(currentLine.length() - 1) == PROPERTY_TEXT_FOLD_BREAK) {
	                        buffer.append(currentLine.substring(0, currentLine.length() - 1));
	                        buffer.append(Constants.NEWLINE);
		                } else {
		                	String entry = currentLine;
		                	if (buffer.length() > 0) {
		                		buffer.append(currentLine);
		                		entry = buffer.toString();
		                		buffer = new StringBuilder();
		                	}
	                        		                	
	                        if (entry.length() > 0) {
	                        	try {
	                        		entry = resolver.resolve(entry);
	                        		if (entry.length() > 0) PropertyInjector.inject(this, entry);
	
	                        	} catch (Exception e) {
	                        		exceptionBundleException.addException(
	                        				new AnnotatedException(e.getMessage(), AnnotatedException.Catagory.ERROR)
	                        						.annotate("line.number", lineNumber));
	                        	}
	                        }                             
		                }
	    
		            } // end if not a comment
	            
	            } // end while	         
	            
	            currentLine = bir.readLine();
	            lineNumber++;
	        }
	        
    	} catch (Exception e) {
    		exceptionBundleException.addException(new AnnotatedException("IO error.", AnnotatedException.Catagory.FAULT, e));
    	}
         
        if (exceptionBundleException.getExceptionBundle().size() > 0) {
        	exceptionBundleException.getExceptionBundle().addFirst(
        			new AnnotatedException("Processing failed.  Giving up.", AnnotatedException.Catagory.FAULT)
	        					.annotate("file", source.peek()));

        	throw exceptionBundleException;
        }
        
        return this;
	}
	
	// ===============================================================================
	// = INTERFACE
	
	public boolean exists(final String... path) {
		String npath = path(path);
		boolean result = properties.containsKey(npath);
		if ((result == false) && (shadows.size() > 0)) {			
			for (HashMap<String, String[]> map : shadows) {
				if (map.containsKey(npath)) {
					result = true;
					break;
				}
			}
		}		
		return result;
	}
		
	public String get(final String path) {		
		String[] values = getMultivalue(path);
		if (values == null) {
			return null;
		} else if (values.length == 1) {
			return values[0];
		} else {
			return PropertyView.encode(values);
		}
	}
	
	public String get(final String... path) {
		return get(path(path));
	}
	
	public String[] getMultivalue(final String path) {
		String npath = fixPathNotEmpty(path);
		String[] result = properties.get(npath);
		if ((result == null) && (shadows.size() > 0)) {			
			for (HashMap<String, String[]> map : shadows) {
				result = map.get(npath);
				if (result != null) break;
			}
		}		
		return result;		
	}
	
	public String[] getMultivalue(final String... path) {
		 return getMultivalue(path(path));
	}
	
	public void set(final String path, final String value) {
		set(path, value, false);
	}
	
	public void set(final String path, final String value, final boolean decode) {
		if (value == null) {
			remove(fixPathNotEmpty(path));
		} else {
			if (decode) {
				properties.put(fixPathNotEmpty(path), PropertyView.decode(value));
			} else {
				String[] values = new String[1];
				values[0] = value;	// Sort of surprised this wasn't boxed by the compiler.
				properties.put(fixPathNotEmpty(path), values);						
			}
		}
	}
	
	public void set(final String path, final String... value) {
		if (value == null) {
			remove(fixPathNotEmpty(path));
		} else {
			properties.put(fixPathNotEmpty(path), value);			
		}	
	}
	
	public void append(final String path, final String value) {
		append(path, value, false);
	}
	
	public void append(final String path, final String value, final boolean decode) {
		String[] values = getMultivalue();
		if (values == null) {
			set(path, value);
		} else {
			String[] decoded;
			if (decode) {
				decoded = PropertyView.decode(value);
			} else {
				decoded = new String[1];
				values[0] = value;
			}
			
			String[] newValues = Arrays.copyOf(values, values.length + decoded.length);
			for (int index = 0; index < decoded.length; index++) {
				newValues[values.length + index] = decoded[index];	
			}			
			set(path, values);
		}
	}
	
	public void append(final String path, final String... value) {
		String[] values = getMultivalue();
		if (values == null) {
			set(path, value);
		} else {
			String[] newValues = Arrays.copyOf(values, values.length + value.length);
			for (int index = 0; index < value.length; index++) {
				newValues[values.length + index] = value[index];
			}
			set(path, newValues);
		}		
	}
	
	public void remove(final String path) {
		properties.remove(fixPathNotEmpty(path));
	}
	
	public void remove(final String... path) {
		remove(path(path));
	}
	
	public void prune(final String path) {
		String npath = normalizePath(path);
		
		// An empty path will clip off the entire tree
		if (npath.length() == 0) {
			initialize();
			
		} else {
			LinkedList<String> killListKeys = new LinkedList<String>();
			for (String itemPath : properties.keySet()) { 
				if (  (itemPath.indexOf(npath) == 0) &&
					  (  (itemPath.length() == npath.length() ) || 
						 ((itemPath.length() > path.length()) && (itemPath.charAt(npath.length() + 1) == PROPERTY_PATH_SEPARATOR) )  
				      )
				   ) {
					killListKeys.add(itemPath);
				}
			}
			
			// We have to do this in stages to avoid concurrent mod exceptions.
			for (String killPath : killListKeys) { 
				properties.remove(killPath);
			}			
		}	
	}
	
	public void prune(final String... path) {
		prune(path(path));
	}
	
	public void graft(PropertyView sourceView) {		
		if (sourceView==null) return;
		String[] value;
		for (String path : sourceView.sub("")) {
			value = sourceView.getMultivalue(path);
			this.set(path, value);
		}		
	}
	
	public PropertyView branch(final String path) {
		return new PropertyStore(properties, fixPath(path));
	}
	
	public PropertyView branch(final String... path) {
		return branch(path(path));
	}

	public Collection<String> sub(final String path) {
		HashSet<String> result = new HashSet<String>();
		String npath = fixPath(path);
		sub(npath, properties, result);
		
		for (HashMap<String, String[]> map : shadows) {
			sub(npath, map, result);
		}		
		return result;
	}
	
	public Collection<String> sub(final String... path) {
		return sub(path(path));
	}
	
	private void sub(final String npath, final HashMap<String, String[]> source, HashSet<String> props) {
		
		// Trivial case for empty path.  For this to happen, root has to be empty too.
		if (npath.length() < 1) {
			for (String kpath : source.keySet()) {
				props.add(kpath);
			}
			
		} else {		
			for (String kpath : source.keySet()) {
				
				if (kpath.length() > npath.length()) {
				
					if ((kpath.indexOf(npath) == 0) &&
						(kpath.charAt(npath.length()) == PROPERTY_PATH_SEPARATOR)	
						) {
						props.add(kpath.substring(npath.length() + 1));
					}
				}			
			}
			
		}
	
	}
	
	
	public Collection<String> ply(final String path) {
		HashSet<String> result = new HashSet<String>();
		String npath = fixPath(path);
		ply(npath, properties, result);
		
		for (HashMap<String, String[]> map : shadows) {
			ply(npath, map, result);
		}		
		return result;		
	}
	
	public Collection<String> ply(final String... path) {
		return ply(path(path));
	}	
	
	private void ply(final String path, final HashMap<String, String[]> source, HashSet<String> props) {
		
		String cpath = fixPathNotEmpty(path) + PROPERTY_PATH_SEPARATOR;
		
		for (String itemPath : source.keySet()) {
		
			if ( (itemPath.indexOf(cpath) == 0) &&
			     (itemPath.length() > cpath.length()) ) {
				
				int mark = itemPath.indexOf(PROPERTY_PATH_SEPARATOR, cpath.length());
				if (mark > 0) {
					props.add(itemPath.substring(0, mark));
				} else {
					props.add(itemPath);
				}
			}
					
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public PropertyView copy(final String path) {
		HashMap<String, String[]> master = (HashMap<String, String[]>) properties.clone();
		for (HashMap<String, String[]> map : shadows) {
			master.putAll(map);
		}		
		return new PropertyStore(master, fixPath(path));
	}
	
	public PropertyView copy(final String... path) {
		return copy(path(path));
	}
	
	public void save() {
		if (source.size() < 1) throw new RuntimeException("Property file not known. Cannot save."); 

		BufferedWriter bir;		
		try {
			bir = new BufferedWriter(new FileWriter(source.peek()));
		} catch (Exception e) {
			throw new AnnotatedException("Could not open property file for writing.", AnnotatedException.Catagory.FAULT, e)
				.annotate("file", source.peek());
		}
		
		try {
			save(bir);
		} catch (AnnotatedException ae) {
			throw ae.annotate("file", source.peek());
		}
	}
	
	public void save(final BufferedWriter bw) {
		save(bw, properties);
		for (HashMap<String, String[]> map : shadows) {
			save(bw, map);
		}
	}
	
	private void save(final BufferedWriter bw, HashMap<String, String[]> source) {
	
		try {
			for (String key : source.keySet()) {
				bw.write(key);
				bw.write(PROPERTY_TEXT_EQUALITY);
				bw.write(PropertyView.encode(source.get(key)));
				bw.newLine();
			}
			bw.newLine();
		
		} catch (Exception e) {
			throw new AnnotatedException("Could not write properties.", AnnotatedException.Catagory.FAULT, e);
			
		} finally {
			try {
				bw.close();
			} catch (Throwable t) {
				// Don't care
			}			
		}
				
	}
	
	public String resolveConfiguration(final String name) {
		return resolveVariable(name);
	}

	public String resolveVariable(final String name) {
		String[] value = getMultivalue(name);
		if (value == null) return "";
		if (value.length == 1) return value[0];
		if (value.length > 1) return PropertyView.encode(value);
		return "";
	}
	
	public void resolveInclude(final String name) {
		if (source.size() < 1) throw new AnnotatedException("Cannot include from a non-file property source");
		File elderFile = new File(source.peek());
		if (! elderFile.canRead()) throw new AnnotatedException("Cannot incude from a non-existant file").annotate("file.path", source.peek());
		
		File baseDir = elderFile.getParentFile();
		File newPropFile = new File(baseDir, name);
		if (! newPropFile.isFile()) throw new AnnotatedException("Included file cannot be found")
			.annotate("file.name", name, "path", newPropFile.getAbsolutePath());
		
		source.push(newPropFile.getAbsolutePath());
		try {
			_load(newPropFile);
			
		} finally {
			source.pop();
		}
	}
	
	public Map<String, String[]> getAll() {
		if (shadows.size() > 0) {
			// Pricey since we to merge into a copy.
			PropertyView view = this.copy("");
			return view.getAll();
			
		} else {
			return properties;			
		}
		
	}
	
	public PropertyStore shadow() {		
		@SuppressWarnings("unchecked")
		LinkedList<HashMap<String, String[]>> copyShadows = (LinkedList<HashMap<String, String[]>>) shadows.clone();
		copyShadows.addFirst(properties);
		@SuppressWarnings("unchecked")
		Stack<String> copySource = (Stack<String>) source.clone();
		return new PropertyStore(root, copySource, copyShadows, new HashMap<String, String[]>());
	}
	
	public ResolverHandler getResolveHandler() {
		return this;
	}

}
