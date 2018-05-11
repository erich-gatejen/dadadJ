package dadad.platform;

import java.io.File;

public class Util {
	
	// ===============================================================================
	// = FIELDS
	
	
	// ===============================================================================
	// = METHODS
	
	public static void cleanDirectory(final File directory) {
	    for (File file : directory.listFiles()) {
	        if (file.isDirectory()) cleanDirectory(file);
	        file.delete();
	    }
	}
	
	
	
}
