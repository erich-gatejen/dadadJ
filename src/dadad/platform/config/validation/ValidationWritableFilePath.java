package dadad.platform.config.validation;

import java.io.File;

/**
 * This is not completely reliable.
 *
 */
public class ValidationWritableFilePath extends Validation {
	
	protected String _validationName() {
		return "a writable file path";
	}

	protected void _validate(final Object data) {

		if (data == null) throw new RuntimeException("Null file path");
		File file = new File(data.toString());
		if (file.exists() && (! file.canWrite())) throw new RuntimeException("Cannot overwrite existing file.");
		if (file.isDirectory()) throw new RuntimeException("Path points to a directory.");
		file = file.getParentFile();
		if (! file.exists()) throw new RuntimeException("File not in a valid directory.");
	
	}

}
