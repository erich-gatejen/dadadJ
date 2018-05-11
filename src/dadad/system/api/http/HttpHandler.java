package dadad.system.api.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import dadad.platform.AnnotatedException;
import dadad.platform.Context;
import dadad.platform.PlatformDataType;
import dadad.platform.Resolver;
import dadad.system.SystemConfiguration;
import dadad.system.api.APIResponse;
import dadad.system.api.APIResponseCodes;

/**
 * Request handler.
 */
public class HttpHandler {
	
	// ===============================================================================
	// = FIELDS
	
	public final static String DEFAULT_FILE = "index.html";
        
	private Context context;
	private File contentDir;
	
	// This may need to be dynamic down the road.
	private HTMLBuilder builder;
	
	// ===============================================================================
	// = METHODS

	public HttpHandler(final Context context) {
		this.context = context;
		
		builder = new HTMLBuilder();
		
		contentDir = new File(context.getRootPath(), context.getConfig().getRequired(SystemConfiguration.CONTENT_DIRECTORY));
		if (! contentDir.isDirectory()) throw new AnnotatedException("Content directy doesnt exist.")
			.annotate("dir.path", contentDir.getAbsolutePath());
	}
		
	public Object handler(final String path, final Map<String, String> parameters) {
		Object result;	
	     // result = new APIResponse(APIResponseCodes.FAULT, APIDataType.TEXT, "Malformed URL.  Not enough path elements. text=[" + path + "]");   
	    	   	
    	try {
    		result = loadAndResolve(path, parameters);
   		
    	} catch (Exception e) {
    		result = new APIResponse(APIResponseCodes.FAULT, PlatformDataType.HTML, "HTTP request failed", e);   
    	}

		return result;
	}
	/*
	public Object handler(final String path, final Map<String, String> parameters, final String resultPage) {
		Object result;	
	    	   	
    	try {
    		result = loadAndResolve(resultPage, parameters);
    		
    	} catch (Exception e) {
    		result = new APIResponse(APIResponseCodes.FAULT, APIDataType.HTML, "HTTP request failed", e);   
    	}

		return result;
	}
	*/
	
	public Object handler(final String path, final Map<String, String> parameters, final String errorText, final Throwable failure, final String errorPage, final boolean debugging) {
		Object result;	
	     // result = new APIResponse(APIResponseCodes.FAULT, APIDataType.TEXT, "Malformed URL.  Not enough path elements. text=[" + path + "]");   
	    	
		String fullErrorText = errorText;
		if (fullErrorText == null) fullErrorText = "An error occured";
		if (failure != null) {
			fullErrorText = fullErrorText + "\r\n\r\n" + AnnotatedException.render(failure, debugging);
		}		
		parameters.put(HttpServer.SPECIAL_ERROR_REPORT_PARAM, fullErrorText);
		
    	try {    		    		
    		result = loadAndResolve(errorPage, parameters);
    		
    	} catch (Exception e) {
    		result = new APIResponse(APIResponseCodes.FAULT, PlatformDataType.HTML, "HTTP request failed while processing an error.r\n\r\n"
    				+ "Original error:" + fullErrorText, e);   
    	}

		return result;
	}
	
	private Object loadAndResolve(final String path, final Map<String, String> parameters) throws Exception {
		Object result;
		
		File theFile = getFile(path);
		if (! theFile.canRead()) {
			result = new APIResponse(APIResponseCodes.NOT_FOUND, PlatformDataType.HTML, "File does not exist: " + theFile.getAbsolutePath());  
		
		} else {
		
			FileType fileType = ContentMapper.lookupFiletypeFromExtension(theFile.getAbsolutePath());
			String mimeType = fileType.mimeType;
			String page = loadFile(theFile);
			
			if (fileType.dataType == PlatformDataType.DTMP) {
				// CACHE THESE LATER
				builder.reset(parameters);
				page = builder.templateResolve(page);			
			} 
    		
    		page = new Resolver(new HttpResolveHandler(context, parameters)).resolve(page);
    				
    		byte[] pageBytes = page.getBytes("UTF-8");
    		result = new HttpResponse(APIResponseCodes.SUCCESS, mimeType, new ByteArrayInputStream(pageBytes), pageBytes.length);   			
		}		
		
		return result;
	}
	
	private File getFile(final String path) {
		String normal = path.trim();
		if ((normal.length() < 1) || ((normal.length() == 1) && (normal.charAt(0) == '/')) ) {
			normal = DEFAULT_FILE;
		}
		
		File theFile = new File(contentDir, normal);
		return theFile;
	}
	
	private String loadFile(final File file) {
		String result;
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
			result = new String(encoded, "UTF-8");
			
		} catch (IOException e) {
			throw new AnnotatedException("Failed to read file to server", e)
				.annotate("file.path", file.getAbsolutePath());
		}
		return result;
	}
	
}