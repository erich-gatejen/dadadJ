package dadad.platform.services;

public class Logger {

	// ===============================================================================
	// = FIELDS
	
	public final static LoggerLevel defaultLevel = LoggerLevel.INFO;
	
	private final LoggerTarget target;
	private final Object tag;
	
	private LoggerLevel currentLevel; 
	
	
	// ===============================================================================
	// = METHODS
	
	public Logger(final LoggerTarget target, final Object tag) {
		this.target = target;
		this.tag = tag;
		currentLevel = defaultLevel;
	}
	
	/**
	 * Get URL to access the log file.
	 * @return the URL or null if the destination is an unidentified stream.
	 */
	public String getUrl() {
		return target.getUrl();
	}
	
	public Logger getFriend(final Object tag) {
		return new Logger(target, tag).setLevel(currentLevel);
	}
	
    public Logger setLevel(final LoggerLevel level) {
        this.currentLevel = level;
        return this;
    }

    public LoggerLevel getLevel() { return this.currentLevel; }

    public boolean isEnabled(final LoggerLevel level) {
        if (currentLevel.ordinal() >= level.ordinal()) {
            return true;
        }
        return false;
    }

    public boolean isDebugging() {
        if (currentLevel.ordinal() >= LoggerLevel.DEBUG.ordinal()) {
            return true;
        }
        return false;
    }
    
    public boolean isTrace() {
        if (currentLevel == LoggerLevel.TRACE) {
            return true;
        }
        return false;
    }
	
    public void log(final LoggerLevel level, final String log) {
    	target.log(level, tag, log);
    }
    
    public void log(final LoggerLevel level, final String log, final Object... values) {
    	target.log(level, tag, log, values);
    }
    
    public void data(final String log) {
    	target.log(LoggerLevel.DATA, tag, log);
    }
    
    public void data(final String log, final Object... values) {
    	target.log(LoggerLevel.DATA, tag, log, values);
    }

    public void data(final String log, final Throwable t) {
        target.log(LoggerLevel.DATA, tag, log, t);
    }

    public void data(final String log, final Throwable t, final Object... values) {
        target.log(LoggerLevel.DATA, tag, log, t, values);    	
    }
    
    public void fault(final String log) {
        if (currentLevel.ordinal() >= LoggerLevel.FAULT.ordinal()) {
        	target.log(LoggerLevel.FAULT, tag, log);
        }
    }
    
    public void fault(final String log, final Object... values) {
        if (currentLevel.ordinal() >= LoggerLevel.FAULT.ordinal()) {
        	target.log(LoggerLevel.FAULT, tag, log, values);
        }
    }

    public void fault(final String log, final Throwable t) {
        if (currentLevel.ordinal() >= LoggerLevel.FAULT.ordinal()) {
        	target.log(LoggerLevel.FAULT, tag, log, t);
        }
    }

    public void fault(final String log, final Throwable t, final Object... values) {
        if (currentLevel.ordinal() >= LoggerLevel.FAULT.ordinal()) {
        	target.log(LoggerLevel.FAULT, tag, log, t, values);
        }    	
    }
    
    public void error(final String log) {
        if (currentLevel.ordinal() >= LoggerLevel.ERROR.ordinal()) {
        	target.log(LoggerLevel.ERROR, tag, log);
        }
    }
    
    public void error(final String log, final Object... values) {
        if (currentLevel.ordinal() >= LoggerLevel.ERROR.ordinal()) {
        	target.log(LoggerLevel.ERROR, tag, log, values);
        }
    }

    public void error(final String log, final Throwable t) {
        if (currentLevel.ordinal() >= LoggerLevel.ERROR.ordinal()) {
        	target.log(LoggerLevel.ERROR, tag, log, t);
        }
    }

    public void error(final String log, final Throwable t, final Object... values) {
        if (currentLevel.ordinal() >= LoggerLevel.ERROR.ordinal()) {
        	target.log(LoggerLevel.ERROR, tag, log, t, values);
        }    	
    }
    
    public void warn(final String log) {
        if (currentLevel.ordinal() >= LoggerLevel.WARN.ordinal()) {
        	target.log(LoggerLevel.WARN, tag, log);
        }
    }
    
    public void warn(final String log, final Object... values) {
        if (currentLevel.ordinal() >= LoggerLevel.WARN.ordinal()) {
        	target.log(LoggerLevel.WARN, tag, log, values);
        }
    }

    public void warn(final String log, final Throwable t) {
        if (currentLevel.ordinal() >= LoggerLevel.WARN.ordinal()) {
        	target.log(LoggerLevel.WARN, tag, log, t);
        }
    }

    public void warn(final String log, final Throwable t, final Object... values) {
        if (currentLevel.ordinal() >= LoggerLevel.WARN.ordinal()) {
        	target.log(LoggerLevel.WARN, tag, log, t, values);
        }    	
    }
    
    public void info(final String log) {
        if (currentLevel.ordinal() >= LoggerLevel.INFO.ordinal()) {
        	target.log(LoggerLevel.INFO, tag, log);
        }
    }
    
    public void info(final String log, final Object... values) {
        if (currentLevel.ordinal() >= LoggerLevel.INFO.ordinal()) {
        	target.log(LoggerLevel.INFO, tag, log, values);
        }
    }

    public void info(final String log, final Throwable t) {
        if (currentLevel.ordinal() >= LoggerLevel.INFO.ordinal()) {
        	target.log(LoggerLevel.INFO, tag, log, t);
        }
    }

    public void info(final String log, final Throwable t, final Object... values) {
        if (currentLevel.ordinal() >= LoggerLevel.INFO.ordinal()) {
        	target.log(LoggerLevel.INFO, tag, log, t, values);
        }    	
    }    
    
    public void debug(final String log) {
        if (currentLevel.ordinal() >= LoggerLevel.DEBUG.ordinal()) {
        	target.log(LoggerLevel.DEBUG, tag, log);
        }
    }
    
    public void debug(final String log, final Object... values) {
        if (currentLevel.ordinal() >= LoggerLevel.DEBUG.ordinal()) {
        	target.log(LoggerLevel.DEBUG, tag, log, values);
        }
    }

    public void debug(final String log, final Throwable t) {
        if (currentLevel.ordinal() >= LoggerLevel.DEBUG.ordinal()) {
        	target.log(LoggerLevel.DEBUG, tag, log, t);
        }
    }

    public void debug(final String log, final Throwable t, final Object... values) {
        if (currentLevel.ordinal() >= LoggerLevel.DEBUG.ordinal()) {
        	target.log(LoggerLevel.DEBUG, tag, log, t, values);
        }    	
    } 
    
    public void trace(final String log) {
        if (currentLevel.ordinal() >= LoggerLevel.TRACE.ordinal()) {
        	target.log(LoggerLevel.TRACE, tag, log);
        }
    }
    
    public void trace(final String log, final Object... values) {
        if (currentLevel.ordinal() >= LoggerLevel.TRACE.ordinal()) {
        	target.log(LoggerLevel.TRACE, tag, log, values);
        }
    }

    public void trace(final String log, final Throwable t) {
        if (currentLevel.ordinal() >= LoggerLevel.TRACE.ordinal()) {
        	target.log(LoggerLevel.TRACE, tag, log, t);
        }
    }

    public void trace(final String log, final Throwable t, final Object... values) {
        if (currentLevel.ordinal() >= LoggerLevel.TRACE.ordinal()) {
        	target.log(LoggerLevel.TRACE, tag, log, t, values);
        }    	
    }     
}
