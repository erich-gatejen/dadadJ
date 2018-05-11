package dadad.data.list;

import dadad.data.model.Block;
import dadad.data.model.Element;

public interface ListerOutput extends Lister {

	/**
	 * Put a block.  It is up to the implementation if it will keep the elements present in the block object.
	 * @param block
	 */
	public void put(final Block block);
	
	/**
	 * Add a element/term for the block.
	 * @param element
	 */
	public void put(final Element element);

	/**
	 * Queries if the implementation saves the original Block elements.
	 * @return true if it saves the elements, otherwise false.
	 */
	public boolean optionBlockElementsAreSaved();
	
}
