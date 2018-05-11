package dadad.system.api.http;

import java.util.HashSet;

import dadad.platform.PlatformDataType;

public class FileType {

	final public PlatformDataType dataType;
	final public String mimeType;
	final public HashSet<String> extensions;
	
	public FileType(final PlatformDataType dataType, final String mimeType, final String... extensions) {
		this.dataType = dataType;
		this.mimeType = mimeType;
		this.extensions = new HashSet<String>();
		for (String item : extensions) {
			this.extensions.add(item);
		}
	}
	
}
