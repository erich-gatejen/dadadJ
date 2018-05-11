package dadad.platform;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;

public class UrlDataFactory {
	
	// ===============================================================================
	// = FIELDS
	
	public final static String URL_PROTOCOL__FILE = "file:";
	
	public enum RecognizedURISchemes {
		HTTP, 
		HTTPS,
		FTP, 
		MAILTO, 
		FILE, 
		DATA,
		URN,
		TELNET,
		ABOUT;
	}
	
	static private class Parts {
		public RecognizedURISchemes scheme;
		public String path;
	}
	
	private final Context context;
	private static final HashMap<String, String> localFilesByContextRoot = new HashMap<String, String>();
	
	// ===============================================================================
	// = METHODS
	
	public UrlDataFactory(final Context context) {
		this.context = context;
	}
	
	/**
	 * Make a url a local file (not a lot of work if it already is a local file).
	 * @param url
	 * @return
	 */
	public File makeLocal(final String url) {
		File result = null;
		
		Parts parts = parse(url);
		switch(parts.scheme) {
		case FILE:
			// already local
			result = new File(parts.path);
			break;
			
		default:
			throw new AnnotatedException("Only FILE: supported.", AnnotatedException.Catagory.FAULT)
				.annotate("file.url", url, "offending.uri.scheme", parts.scheme.name());			
		}
		
		return result;
	}
	
	/**
	 * Only supporting files for now.
	 * @param url
	 * @return
	 */
	public static BufferedReader getReader(final String url) {
		BufferedReader br = null;
		
		Parts parts = parse(url);
		switch(parts.scheme) {
		case FILE:
			if (parts.path.trim().length() < 1) 
				throw new AnnotatedException("Failed to open file.  Bad URL.  No path included.", AnnotatedException.Catagory.FAULT)
					.annotate("file.url", url);			
			
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(parts.path.trim())));
				
				// Files sometimes have a BOM.  Ditch it.
				br.mark(1);
		        char[] candidate = new char[1];
		        br.read(candidate);
		        if (candidate[0] != '\ufeff') {
		        	br.reset();
		        }				

				
			} catch (Exception e) {
				throw new AnnotatedException("Failed to open file.", AnnotatedException.Catagory.FAULT, e)
						.annotate("file.url", url);
			}			
			break;
			
		default:
			throw new AnnotatedException("Only FILE: supported.", AnnotatedException.Catagory.FAULT)
				.annotate("file.url", url, "offending.uri.scheme", parts.scheme.name());			
		}
		
		return br;
	}
	
	/**
	 * Only supporting files for now.
	 * @param url
	 * @param append
	 * @return
	 */
	public static BufferedWriter getWriter(final String url, final boolean append) {
		BufferedWriter br = null;
		String urlNormal = url;
		
		// HAX: protect the user
		if (url.startsWith("file:///")) {
			if (url.length() > 8)
				urlNormal = "file://" + url.substring(8);
			else
				urlNormal = "file://";   // Which will likely cause an exception...	
		}
		
		Parts parts = parse(urlNormal);
		switch(parts.scheme) {
		case FILE:
			if (parts.path.trim().length() < 1) 
				throw new AnnotatedException("Failed to open file.  Bad URL.  No path included.", AnnotatedException.Catagory.FAULT)
					.annotate("file.url", urlNormal);			
			
			try {
				br = new BufferedWriter(new FileWriter(parts.path.trim(), append));
								
			} catch (Exception e) {
				throw new AnnotatedException("Failed to open file for writing.", AnnotatedException.Catagory.FAULT, e)
						.annotate("file.url", urlNormal);
			}			
			break;
			
		default:
			throw new AnnotatedException("Only FILE: supported.", AnnotatedException.Catagory.FAULT)
				.annotate("file.url", urlNormal, "offending.uri.scheme", parts.scheme.name());			
		}
		
		return br;
	}
	
	/**
	 * Get the URI scheme.  
	 * @param uri
	 * @return
	 */
	private static Parts parse(final String uri) {
		Parts result = new Parts();
		result.path = uri;
		result.scheme = RecognizedURISchemes.FILE;	// Unrecognized schemes are assumed to be files.		
		
		int firstColon = uri.indexOf(':');
		if (firstColon > 0) {
			
			try {
				result.scheme = RecognizedURISchemes.valueOf(uri.substring(0, firstColon).toUpperCase());

				
			} catch (Exception e) {
				// Don't care.  Assume it is a file.
			}
			
			if (firstColon < (uri.length() - 1)) {
				result.path = uri.substring(firstColon + 1);
			} else {
				result.path = "";
			}
		
		} 
		
		return result;
	}
	
	// ===============================================================================
	// = PACKAGE METHODS
	
	static void cleanLocals(final Context localContext) {
		// just a stub now.
	}
	
}
