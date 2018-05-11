package dadad.data.model;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Data id generator.
 *
 * The 32 lsb are reserved for document id.  The 32 msb are reserved for block/term id, which is also a union with
 * the document
 * id.
 */
public class DataId implements Serializable {

    final static long serialVersionUID = 1;

    public final static long NO_ID = 0;
    public final static long STARTING_DOC_ID = 1;
    public final static long STARTING_ORDINAL = 0x00000001FFFFFFFFL;
    public final static long MASK_BLOCK_ID = 0x00000000FFFFFFFFL;
    public final static DataId NO_DOCUMENT_ID = new DataId(NO_ID);

	// ===============================================================================
	// = FIELDS

	private final long documentId;

	private AtomicLong currentOrdinal = new AtomicLong(STARTING_ORDINAL);

	// ===============================================================================
	// = METHODS

	public DataId(final long documentId) {

		this.documentId = documentId;
	}

	public long getDocumentId() {
	    return documentId;
    }

    public static long getDocumentId(final long id) {
		return id & MASK_BLOCK_ID;
	}

    public static long getBlockId(final long documentId, final long blockOrdinalId) {
        return documentId & blockOrdinalId;
    }

	public static long getBlockOrdinalId(final long id) {
	    return id >> 32;
    }

	public long getNewBlockId() {
        return documentId + currentOrdinal.get();
	}

	public void next() {
        currentOrdinal.getAndIncrement();
    }

    public static boolean isNoId(final long id) {
	    if (id == NO_ID) return true;
	    return false;
    }

    public boolean isNoId() {
	    if (documentId == NO_ID) return true;
	    return false;
    }
}
