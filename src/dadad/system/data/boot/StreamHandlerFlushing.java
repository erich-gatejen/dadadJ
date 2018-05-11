package dadad.system.data.boot;

import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class StreamHandlerFlushing extends StreamHandler {
	
	public StreamHandlerFlushing() {
		super();
	}

	public StreamHandlerFlushing(OutputStream out, Formatter formatter) {
		super(out, formatter);
	}
	
    @Override
    public synchronized void publish(LogRecord record) {
    	super.publish(record);
    	super.flush();
    }
	
}
