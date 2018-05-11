package dadad.system.api.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import dadad.platform.AnnotatedException;
import dadad.platform.PlatformDataType;
import dadad.platform.services.Logger;
import dadad.system.SystemInterface;
import dadad.system.WorkKernel;
import dadad.system.api.APIImpl;

/**
 * Server api implementation.
 */
public class ServerAPI_SYSTEM extends APIImpl {

	// ===============================================================================
	// = FIELDS
	
	public final static long PERSISTENCE_LIMIT_MS = 60 * 30 * 1000;  // Half hour
	
	
	// ===============================================================================
	// = ABSTRACT
	
	public long persistanceLimit() {
		return PERSISTENCE_LIMIT_MS;
	}
	
	// ===============================================================================
	// = METHOD
	
	public String load(final PlatformDataType dataType, final String filePath) {
		
		String value = "";
		
		// Does the full path find the file?  Honestly, this might be a titanic security risk.  
		// TODO: evaluate the security implications.
		String actualPath = filePath;
		File sourceFile = new File(actualPath);
		if (! sourceFile.exists()) {
			actualPath = context.getRootPath() + File.separatorChar + filePath;
			sourceFile = new File(actualPath);
		}
				
		try {
			if (! sourceFile.canRead()) throw new AnnotatedException("Cannot load text because file cannot be read.");				
			
			byte[] encoded;
			try {
				encoded = Files.readAllBytes(Paths.get(actualPath));
				value = new String(encoded, "UTF-8");
				
			} catch (IOException e) {
				throw new AnnotatedException("Failed to load text from file.", e);
			}
			
		} catch (AnnotatedException ae) {
			throw ae.annotate("file.path", filePath, "actual.path", actualPath);
		}
		
		return value;
	}

	public String save(final PlatformDataType dataType, final String filePath, final String text) {
		
		String actualPath = context.getRootPath() + File.separatorChar + filePath;
		try {
			
			try {
				
				String value = text;
				if (value == null) value = "";
				
				byte[] bytes = value.getBytes("UTF-8");
				Files.write(Paths.get(actualPath), bytes, StandardOpenOption.CREATE);
				
			} catch (IOException e) {
				throw new AnnotatedException("Failed to save text into file.", e);
			}
			
		} catch (AnnotatedException ae) {
			throw ae.annotate("file.path", filePath, "actual.path", actualPath);
		}
		
		return text;
	}
	
	public String reportlog(final PlatformDataType dataType, final String workProcessName) {		
		
		String url = null;
		try {
			
			SystemInterface si = WorkKernel.getSystemInterface();
			Logger logger = si.getReportLoggerByName(workProcessName);
			if (logger != null) url = logger.getUrl();
			if (url == null) url = "";
			
		} catch (Exception e) {
			throw new AnnotatedException("Failed to get .", e);
		}
		
		return url;
	}
	
	
}
