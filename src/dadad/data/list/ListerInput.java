package dadad.data.list;

import dadad.data.model.Block;

public interface ListerInput extends Lister {

	/**
	 * Get a block.  All elements will be members of a block, so yu must get a block before trying to get an element.
	 * The elements within the Block object may or may not be present--they they are present, they will be the elements from
	 * the original source.
	 * <p>
	 * After calling Block, you can call getElement until you deplete all the elements in the block.
     * @param ownerId the id for the new owner of this block.
	 * @return a block or null if there are no more.
	 * @see dadad.data.model.Block
	 */
	public Block getBlock(final long ownerId);
	
	/**
	 * Queries if the implementation if the original Block terms are present in the blocks.
	 * @return true if they are present, otherwise false.
	 */
	public boolean optionBlockTermsArePresent();

}
