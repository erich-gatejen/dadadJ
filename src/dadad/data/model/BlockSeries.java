package dadad.data.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates a series of blocks where the block ids are ordinal.
 */
public class BlockSeries {

	// ===============================================================================
	// = FIELDS

	private AtomicLong ordinal = new AtomicLong(BlockInfo.STARTING_BLOCK_ID);
	private final BlockType blockType;
	private final long ownerId;

    // ===============================================================================
    // = METHODS

    public BlockSeries(final BlockType blockType, final long ownerId) {
        this.blockType = blockType;
        this.ownerId = ownerId;
    }

    public BlockInfo next() {
        return new BlockInfo(blockType, ownerId, ordinal.incrementAndGet());
    }

    public BlockInfo next(final BlockType blockType) {
        return new BlockInfo(blockType, ownerId, ordinal.getAndIncrement());
    }

	public static BlockSeries getSeries(final BlockType blockType, final long ownerId) {
        return new BlockSeries(blockType, ownerId);
    }
}
