package dadad.platform;

import java.io.File;

import dadad.platform.config.Configuration;

/**
 * General context base.
 */
public abstract class Context {

    // ===============================================================================
    // = ABSTRACT

    abstract public Configuration getConfig();

    abstract public void setYieldable(final Yieldable yieldable);

    abstract public void yield();

    abstract public PropertyView copyProperties();

    abstract public Context copyContext();

    abstract public Context subContext();

    abstract public Resolver getResolver();

    abstract public String getRootPath();

    abstract public File getTempDirFile();

    abstract public File getShareDirTempFile();

    abstract public File getDataDirFile();


    // ===============================================================================
    // = DATA

    public final static String TEMP_FILE_SUFFIX = ".tmp";

    public final static String SUBDIRECTORY_TEMP = "temp";
    public final static String SUBDIRECTORY_DATA = "data";
    public final static String SUBDIRECTORY_DATA_DEFAULT = "default";

    protected Configuration config;
    protected PropertyView properties;
    protected File tempDirectory;
    protected File dataDirectory;

    // ===============================================================================
    // = METHODS

    public Context(final PropertyView properties) {
        this.properties = properties;
        config = new Configuration(properties, false, false);
        tempDirectory = new File(getRootPath(), SUBDIRECTORY_TEMP);
        if (! tempDirectory.isDirectory()) throw new AnnotatedException("Temporary directory is not present.")
                .annotate("temp.dir.path", tempDirectory.getAbsolutePath());
        dataDirectory = new File(getRootPath(), SUBDIRECTORY_DATA);
        if (! dataDirectory.isDirectory()) throw new AnnotatedException("Data directory is not present.")
                .annotate("data.dir.path", dataDirectory.getAbsolutePath());
    }

    // Subcontext
    protected Context(final Context context) {
        this.properties = context.properties.shadow();
        this.config = new Configuration(this.properties, false, false);
        this.tempDirectory = context.tempDirectory;
        this.dataDirectory = context.dataDirectory;
    }

}

