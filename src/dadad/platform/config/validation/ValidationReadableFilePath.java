package dadad.platform.config.validation;

import java.io.File;

/**
 * Validate the path represents a file that can be read.  This is more reliable than verifying a writable file path,
 * since the OS and security manager will verify it can actually be read.
 *
 */
public class ValidationReadableFilePath extends Validation {
	
	protected String _validationName() {
		return "a readable file path";
	}

	protected void _validate(final Object data) {

		if (data == null) throw new RuntimeException("Null file path");
		File file = new File(data.toString());
        if (file.isDirectory()) throw new RuntimeException("Path points to a directory.");
		if (! file.exists()) throw new RuntimeException("File does not exist.");
        if (! file.canRead()) throw new RuntimeException("File cannot be read.");
	}

}
