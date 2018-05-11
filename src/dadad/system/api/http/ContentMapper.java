package dadad.system.api.http;

import java.util.HashMap;

import dadad.platform.AnnotatedException;
import dadad.platform.PlatformDataType;

public class ContentMapper {
	
	
	// ===============================================================================
	// = FIELDS
	
	public final static FileType DEFAULT_TYPE = new FileType(PlatformDataType.DATA, "application/octet-stream", "exe", "bin");
	
	
	// ===============================================================================
	// = METHODS
	
	static {
		buildExtentionMap();
	}
	
	public static FileType lookupFiletypeFromExtension(final String path) {
		FileType result = null;
		if (path != null) { 
			try {
				String clipped = path;
				int index = path.lastIndexOf('.');
				if (index > 0) clipped = path.substring(index + 1);
				index = path.lastIndexOf('.');
				if (index >= 0) {
					clipped = path.substring(index + 1);
					result = extentionMap.get(clipped.toLowerCase());
				}

			} catch (Exception e) {
				// Type can't be determined, so let it be a binary.
			}
		}
		
		if (result == null) result = DEFAULT_TYPE;
		return result;
	}
	
	// ===============================================================================
	// = INTERNAL
	
	private static HashMap<String, FileType> extentionMap;
	
	private static synchronized void buildExtentionMap() {
		if (extentionMap == null) {
			extentionMap = new HashMap<String, FileType>();

			// CUSTOM DADAD TEMPLATE
			putMap(new FileType(PlatformDataType.DTMP, "text/html", "dtemp"));
			
			// Borrowed from THINGS
			putMap(new FileType(PlatformDataType.DATA, "application/octet-stream", "exe", "bin"));
			
			putMap(new FileType(PlatformDataType.TEXT, "text/plain", "txt", "text", "c", "asm", "java", "h", "cpp", "hpp", "ini"));
			putMap(new FileType(PlatformDataType.DATA, "text/rtf", "rtf"));
			putMap(new FileType(PlatformDataType.DATA, "application/msword", "doc", "docx"));
			putMap(new FileType(PlatformDataType.DATA, "application/excel", "xls"));
			putMap(new FileType(PlatformDataType.DATA, "application/vnd.ms-powerpoint", "ppt"));
			putMap(new FileType(PlatformDataType.DATA, "application/pdf", "pdf"));

			putMap(new FileType(PlatformDataType.HTML, "text/html", "html", "htm", "xhtml"));
			putMap(new FileType(PlatformDataType.TEXT, "text/xml", "xml"));
			putMap(new FileType(PlatformDataType.TEXT, "text/css", "css"));
			putMap(new FileType(PlatformDataType.TEXT, "application/xml-dtd", "dtd"));
			
			putMap(new FileType(PlatformDataType.DATA, "application/java", "class"));
			putMap(new FileType(PlatformDataType.DATA, "application/java-archive", "jar"));
			putMap(new FileType(PlatformDataType.DATA, "application/zip", "zip"));

			putMap(new FileType(PlatformDataType.TEXT, "message/rfc822", "eml"));
			
			putMap(new FileType(PlatformDataType.DATA, "image/gif", "gif"));
			putMap(new FileType(PlatformDataType.DATA, "image/x-vga-bitmap", "vga"));
			putMap(new FileType(PlatformDataType.DATA, "image/jpeg", "jpg", "jpeg", "jpe", "jpm"));
			putMap(new FileType(PlatformDataType.DATA, "image/png", "png"));			
			
			putMap(new FileType(PlatformDataType.DATA, "video/mp4", "mp4"));	
			putMap(new FileType(PlatformDataType.DATA, "video/mpeg", "mpeg", "mpg"));	
			putMap(new FileType(PlatformDataType.DATA, "video/ogg", "ogg", "ogg"));	

			putMap(new FileType(PlatformDataType.DATA, "audio/mpeg", "mp3"));	
			putMap(new FileType(PlatformDataType.DATA, "audio/wave", "wav"));	

		}
	}
	
	private static void putMap(final FileType type) {
		for (String item : type.extensions) {
			String extensionNormal = item.toLowerCase();
			if (extentionMap.containsKey(extensionNormal)) 
				throw new AnnotatedException("Type extention already used")
					.annotate("extention", extensionNormal, "existing.type", extentionMap.get(item).toString());
			extentionMap.put(extensionNormal, type);
		}
	}

}
