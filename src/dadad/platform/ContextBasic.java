package dadad.platform;

import dadad.platform.config.Configuration;
import dadad.platform.config.ContextConfiguration;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

/**
 * General context.  A context is considered unique if it has a unique ContextConfiguration.CONTEXT_ROOT_PATH.
 */
public class ContextBasic extends Context {

	// ===============================================================================
	// = FIELDS

	private final AtomicLong index = new AtomicLong();

	private File shareDirectory;

	private Yieldable yieldable;

    // ===============================================================================
    // = METHODS

    public ContextBasic(final PropertyView properties) {
        super(properties);
    }

    public ContextBasic(final Context context) {
        super(context);
    }

	// ===============================================================================
	// = ABSTRACT
		
	public Configuration getConfig() {
		return config;
	}
		
	public void setYieldable(final Yieldable yieldable) {
		this.yieldable = yieldable;
	}
	
	public void yield() {
		if (yieldable != null) yieldable.doYield();
	}
	
	public PropertyView copyProperties() {
		return properties.copy("");
	}
	
	public Context copyContext() {
		return new ContextBasic(copyProperties());
	}
	
	// You must override this and call from subclass first!
	public Context subContext() {
		return new ContextBasic(this);
	}
	
	public Resolver getResolver() {
		return new Resolver(properties.getResolveHandler());
	}

	public String getRootPath() {
		return config.getRequired(ContextConfiguration.CONTEXT_ROOT_PATH);
	}
	
	public File getTempDirFile() {
		return new File(tempDirectory, "" + System.currentTimeMillis() + "_" + index.getAndIncrement() + TEMP_FILE_SUFFIX);		
	}
	
	public synchronized File getShareDirTempFile() {
		if (shareDirectory == null) {
			shareDirectory = new File(config.getRequired(ContextConfiguration.CONTEXT_SHARE_PATH));
			if (! shareDirectory.isDirectory()) throw new AnnotatedException("Share directory is not present.")
				.annotate(ContextConfiguration.CONTEXT_SHARE_PATH.property(), shareDirectory.getAbsolutePath());	
			
		}		
		return new File(shareDirectory, "" + System.currentTimeMillis() + "_" + index.getAndIncrement() + TEMP_FILE_SUFFIX);		
	}
	
	public File getDataDirFile() {
		String run = config.get(ContextConfiguration.CONTEXT_RUN);
		if ((run == null) || (run.trim().length() < 1)) run = SUBDIRECTORY_DATA_DEFAULT;
		File dataDir = new File(dataDirectory, run);
		
		try {
			if (dataDir.isDirectory()) return dataDir;
			if (dataDir.isFile()) throw new AnnotatedException("Proposed data directory exists as a file");				
			if (! dataDir.mkdirs()) throw new AnnotatedException("Could not make proposed data directory.");
		
		} catch (AnnotatedException ae) {
			throw ae.annotate("proposed.data.dir", dataDir.getAbsolutePath());
		}
		
		return dataDir;
	}
	
	// ===============================================================================
	// = END OF LIFE
	
	protected void finalize() throws Throwable {
	     try {
	    	 UrlDataFactory.cleanLocals(this);
	    	 
	     } finally {
	         super.finalize();
	     }
	 }
	
}

