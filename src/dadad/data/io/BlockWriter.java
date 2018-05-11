package dadad.data.io;

import java.io.BufferedWriter;
import java.io.Writer;

import dadad.data.model.Block;
import dadad.platform.config.Configuration;

public abstract class BlockWriter {

	// ===============================================================================
	// = FIELDS
	
	protected final BufferedWriter writer;
	protected final Configuration config;
	
	// ===============================================================================
	// = METHODS
	
	public BlockWriter(final Writer writer, final Configuration config) {
		this.config = config;
		
		if (writer instanceof BufferedWriter) {
			this.writer = (BufferedWriter) writer;
		} else {
			this.writer = new BufferedWriter(writer);
		}
	}
		
	// ===============================================================================
	// = ABSTRACT

	abstract public void write(final Block block);
	
}
