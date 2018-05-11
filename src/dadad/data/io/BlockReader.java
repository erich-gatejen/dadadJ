package dadad.data.io;

import java.io.Reader;

import dadad.data.model.Block;
import dadad.data.model.Document;
import dadad.platform.config.Configuration;

/**
 * No block reader should be thread safe.
 */
public abstract class BlockReader {

	// ===============================================================================
	// = FIELDS
	
	private final Document document;
	private final Reader reader;
	protected final Configuration config;
	
	// ===============================================================================
	// = METHODS
	
	public BlockReader(final Reader reader, final Document document, final Configuration config) {
		this.reader = reader;
		this.document = document;
		this.config = config;
	}
	
	/**
	 * Read blocks until the document input is drained.
	 * @return the block or null if drained.
	 */
	public Block read() {
		Block block = readBlock(document.documentId.getNewBlockId(), document.documentId.getDocumentId());
		if (block != null) document.documentId.next();
		return block;
	}
	
	protected long documentId() {
		return document.documentId.getDocumentId();
	}
	
	protected Reader getReader() {
		return reader;
	}
	
	public Document getDocument() {
		return document;
	}
	
	// ===============================================================================
	// = ABSTRACT

	abstract protected Block readBlock(final long blockId, final long ownerId);
	
}
