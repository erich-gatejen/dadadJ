package test;

import dadad.platform.*;
import dadad.platform.config.Configuration;
import dadad.platform.config.ContextConfiguration;
import dadad.platform.services.Logger;
import dadad.platform.services.LoggerLevel;
import dadad.platform.services.LoggerTarget;
import dadad.platform.services.PlatformInterface;
import dadad.system.WorkKernel;
import dadad.system.WorkProcess;

import java.io.*;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Context stub for testing.
 */
public class TestContext extends Context implements PlatformInterface {
	
	// ===============================================================================
	// = FIELDS

	private final AtomicLong index = new AtomicLong();

	
	// ===============================================================================
	// = METHODS

	public TestContext(final PropertyView properties) {
		super(properties);
	}
	
	// Subcontext
	protected TestContext(final TestContext context) {
		super(context);
	}
		
	public Configuration getConfig() {
		return config;
	}
		
	public void setYieldable(final Yieldable yieldable) {
		// NOP
	}
	
	public void yield() {
		// NOP
	}
	
	public PropertyView copyProperties() {
		return properties.copy("");
	}
	
	public TestContext copyContext() {
		return new TestContext(copyProperties());
	}
	
	// You must override this and call from subclass first!
	public TestContext subContext() {
		return new TestContext(this);
	}
	
	public Resolver getResolver() {
		return new Resolver(properties.getResolveHandler());
	}
	
	public String getRootPath(){
        return config.getRequired(ContextConfiguration.CONTEXT_ROOT_PATH);
	}

	public File getTempDirFile() {
		throw new Error("Not supported in this test stub.");
	}
	
	public synchronized File getShareDirTempFile() {
		throw new Error("Not supported in this test stub.");
	}
	
	public File getDataDirFile() {
		throw new Error("Not supported in this test stub.");
	}


    // ===============================================================================
    // = INTERFACE

    private HashMap<String, Logger> reportLoggers;
	private Logger systemLogger;

    public Logger getReportLogger(final Object tag) {
        Logger result = reportLoggers.get(tag);
        if (result == null) {
            PrintWriter pw = new PrintWriter(System.out);
            LoggerTarget target = new LoggerTarget(pw, "stdout");
            result = new Logger(target, tag.toString()).setLevel(LoggerLevel.INFO);
        }
        return result;
    }

    public Logger getLogger() {
        if (systemLogger == null) {
            PrintWriter pw = new PrintWriter(System.out);
            LoggerTarget target = new LoggerTarget(pw, "stdout");
            systemLogger = new Logger(target, "SYSTEM").setLevel(LoggerLevel.INFO);
        }
        return systemLogger;
    }

    public Context getContext() {
	    return this;
    }


}

