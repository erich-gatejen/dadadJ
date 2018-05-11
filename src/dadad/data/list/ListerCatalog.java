package dadad.data.list;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.Writer;

import dadad.platform.AnnotatedException;
import dadad.platform.Constants;

public enum ListerCatalog {

	VERSION__TABULAR1(ListerOutputTabular1.class, ListerInputTabular1.class);
	
	private final Class<?> outputClass;
	private final Class<?> inputClass;
	private ListerCatalog(final Class<?> outputClass, final Class<?> inputClass) {
		this.outputClass = outputClass;
		this.inputClass = inputClass;
	}
	
	public String getVersion() {
		return this.name();
	}
	
	public Class<?> getOutputClass() {
		return outputClass;
	}

	public Class<?> getInputClass() {
		return inputClass;
	}
	
	public static ListerCatalog getListerEntry(final String version) {
		try {
			return ListerCatalog.valueOf(version);
		} catch (Throwable t) {
			throw new AnnotatedException("Unsupported version name for a lister.")
					.annotate("version.name", version);
		}
	}
	
	/**
	 * A terminated line containing the version string can be used to identify the list version.
	 * @param source
	 * @return the lister catalog entry.
	 */
	public static ListerCatalog getListerEntry(final Reader source) {
		ListerCatalog result = null;
		try {
			
			BufferedReader br;
			if (source instanceof BufferedReader) {
				br = (BufferedReader) source;
			} else {
				br = new BufferedReader(source);
			}
			
			String versionString = br.readLine();
			if (versionString == null) throw new AnnotatedException("Failed to read version line from source because the source was empty.");
			result = getListerEntry(versionString.trim());
						
		} catch (AnnotatedException ae) {
			throw ae;
			
		} catch (Exception e) {
			throw new AnnotatedException("Failed to read valid version line from source.", e);			
		}
		
		return result;
	}
		
	public ListerOutput getOutputLister(final Writer target) {
		ListerOutput result = null;
		
		try {
		
			if (outputClass == null) throw new AnnotatedException("Lister does not support output.");
			if (WritingLister.class.isAssignableFrom(outputClass)) {
				if (target == null) throw new AnnotatedException("Lister requires a target Writer.");					
			} else {
				if (target != null) throw new AnnotatedException("Lister does not support writing to a target, but one was provided in this call.");					
			}
			
			try {
				result = (ListerOutput) outputClass.newInstance();
				
				if (target != null) {
					((WritingLister) result).setTarget(target);
				}
				
				// Push version
				target.write(this.name());
				target.write(Constants.NEWLINE);
			
			} catch (Exception e) {
				throw new AnnotatedException("Failed to create output lister.", e);
			}
		
		} catch (AnnotatedException ae) {
			throw ae.annotate("lister.version", getVersion());
		}
		
		return result;
	}
	
	public ListerInput getInputLister(final Reader target) {
		ListerInput result = null;
		
		try {
		
			if (inputClass == null) throw new AnnotatedException("Lister does not support input.");
			if (ReadingLister.class.isAssignableFrom(inputClass)) {
				if (target == null) throw new AnnotatedException("Lister requires a target Reader.");					
			} else {
				if (target != null) throw new AnnotatedException("Lister does not support reading from a target, but one was provided in this call.");					
			}
			
			try {
				result = (ListerInput) inputClass.newInstance();
				
				if (target != null) {
					((ReadingLister) result).setTarget(target);
				}
			
			} catch (Exception e) {
				throw new AnnotatedException("Failed to open list.", e);
			}
		
		} catch (AnnotatedException ae) {
			throw ae.annotate("lister.version", getVersion());
		}
		
		return result;
	}
	
}