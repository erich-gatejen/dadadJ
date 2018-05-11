package dadad.data.list;


import dadad.data.model.BlockType;
import dadad.data.model.ElementType;

/**
 * Common to all listers or list readers.
 */
public interface ListerCommon {

	// ===============================================================================
	// = POSITIONAL FIELDS

	public final static String COMMENT_CHARACTER = "#";

	public final static String ID_BLOCK_LABEL = "BLOCK";
	public final static String ID_ELEMENT_LABEL = "E";

    /**
     * Slot positions for a Block entry.
     */
	enum BlockSlots {
        LABEL(0, String.class, true),
        ID(1, Long.class, true),
        ORDINAL(2, Long.class, true),
        TYPE(3, BlockType.class, true),
        META__SIZE(4, Long.class, false);

        public final int slot;
        public final Class<?> clazz;
        public final boolean required;
        private BlockSlots(final int slot, final Class<?> clazz, final boolean required) {
           this.slot = slot;
           this.clazz = clazz;
           this.required = required;
        }
    }

    /**
     * Slot positions for a Block entry.
     */
    enum TermSlots {
        BLANK(0, String.class, true),
        LABEL(1, String.class, true),
        TYPE(2, ElementType.class, true),
        TAG(3, String.class, true),
        TEXT(4, BlockType.class, true),
        META__SIZE(4, Long.class, false);

        public final int slot;
        public final Class<?> clazz;
        public final boolean required;
        private TermSlots(final int slot, final Class<?> clazz, final boolean required) {
            this.slot = slot;
            this.clazz = clazz;
            this.required = required;
        }
    }


}
