package dadad.data.model;

/**
 * Information about the block.
 */
public class BlockInfo {
	
	// ===============================================================================
	// = FIELDS
	//
	//  - BlockType : IPS : type
    //  - blockId   : KPS : ordinal starting with first block (1) or UNSET_BLOCK_ID - it should be document unique
	//  - ownerId   : iPs : doc or block id.
    //  - start     :  Ps : starting offset in document--inclusive.  Or UNKNOWN (negative number)
    //  - end       :  Ps : ending offset in document--exclusive.  Or UNKNOWN (negative number)

	public final static long STARTING_BLOCK_ID = 1;
	public final static long UNSET_OWNER_ID = -1;
	public final static long UNSET_BLOCK_ID = -1;
	
	private final BlockType blockType;
	private final long blockId;
	private final long ownerId;
	private long start;
	private long end;

    // ===============================================================================
	// = METHODS
	
	// Used for bookends.  Don't use it elsewhere.
	public BlockInfo() {
		this(BlockType.INTERNAL, UNSET_OWNER_ID, UNSET_BLOCK_ID);
	}
	
	public BlockInfo(final BlockType blockType, final long ownerId) {
		this(blockType, ownerId, UNSET_BLOCK_ID);
	}
	
	public BlockInfo(final BlockType blockType, final long ownerId, final long blockId) {
		this.blockType = blockType;
		this.ownerId = ownerId;
		this.blockId = blockId;
	}
	
	public BlockType type() {
		return blockType;
	}
	
	public long ownerId() {
		return ownerId;
	}
	
	public long blockId() {
		return blockId;
	}
	
	public long start() {
		return start;
	}
	
	public long end() {
		return end;
	}
	
	public BlockInfo bounded(final long start, final long end) {
		this.start = start;
		this.end = end;
		return this;
	}
	
	public String render() {
		return "type=" + blockType.name() + ", ownerId=" + ownerId + ", blockId=" + blockId;
	}

	public boolean isStartingBlock() {
	    if (blockId == STARTING_BLOCK_ID) return true;
        return false;
    }

}
