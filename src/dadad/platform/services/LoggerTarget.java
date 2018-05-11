package dadad.platform.services;	 

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import dadad.platform.AnnotatedException;

public class LoggerTarget {

	// ===============================================================================
	// = FIELDS
	
	public final static String NULL_FLAG = "null";
	public final static char SEPARATOR_CHARACTER = '|';
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
	
	private final PrintWriter pw;
	private final String url;
	
	// ===============================================================================
	// = METHODS
	
	/**
	 * Logger target constructor.
	 * @param pw
	 * @param url pass null if it is an unidentified stream output.
	 */
	public LoggerTarget(final PrintWriter pw, final String url) {
		this.pw = pw;
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
    public String log(final LoggerLevel level, final Object tag, final String log) {
    	return log(level, tag, log, null, (Object[]) null);
    }
	
    public String log(final LoggerLevel level, final Object tag, final String log, final Object... values) {
    	return log(level, tag, log, null, values);
    }
	
    public String log(final LoggerLevel level, final Object tag, final String log, final Throwable t) {
    	return log(level, tag, log, t, (Object[]) null);
    }
	
    public String log(final LoggerLevel level, final Object tag, final String log, final Throwable t, final Object... values) {
        StringBuilder sb = new StringBuilder();
        sb.append(level.text());
        sb.append(SEPARATOR_CHARACTER);
        sb.append(format.format(new Date()));
        sb.append(SEPARATOR_CHARACTER);
        sb.append(tag.toString());
        sb.append(SEPARATOR_CHARACTER);
        sb.append(log);

        if (values != null) {
            for (int pair = 0; pair < values.length; pair += 2) {
                if ((pair + 1) == values.length) {
                    sb.append(SEPARATOR_CHARACTER);
                    sb.append(values[pair]);
                    
                } else {
                    sb.append(SEPARATOR_CHARACTER);

                    if (values[pair] != null) sb.append(values[pair].toString());
                    else sb.append(NULL_FLAG);

                    sb.append("=[");

                    if (values[pair + 1] != null) sb.append(values[pair + 1].toString());
                    else sb.append(NULL_FLAG);

                    sb.append(']');
                }
            }
        }
        
        if (t != null) {
            sb.append(SEPARATOR_CHARACTER);       	
            sb.append("exception=[");
            sb.append(AnnotatedException.render(t));
            sb.append(']');
        }

        String result = sb.toString();
        pw.write(result);
        pw.println();
        pw.flush();

        return result;
    }
	
	public void close() {
		pw.close();
	}
	
}
