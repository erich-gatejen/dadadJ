package dadad.platform;

import java.io.IOException;
import java.io.Reader;

public class LimitedReader extends Reader {

	private final Reader reader;
	private long limit;
	
	public LimitedReader(final Reader reader, final long limit) {
		this.reader = reader;
		this.limit = limit;
	}
	
	@Override
	public int read(java.nio.CharBuffer target) throws IOException {
		throw new RuntimeException("Not supported yet.");
	}
	
	@Override
	public int read() throws IOException { 
		if (limit < 1) return -1;
		limit--;
		return reader.read();		
	}

	@Override
	public int read(char cbuf[], int off, int len) throws IOException {
		if (len > limit) {
			int actual = reader.read(cbuf, off, len);
			limit = limit - actual;
			return actual;
			
		} else {
			limit -= len;
			return reader.read(cbuf, off, len);
		}
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}
	
}
