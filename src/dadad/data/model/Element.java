package dadad.data.model;

import dadad.platform.Truth;

import java.io.Serializable;

/**
 * Element.
 */
public class Element implements Serializable, Cloneable, Comparable<Element>  {
	
	final static long serialVersionUID = 1;
	
	public final static Element rejectedElement;
	static {
		rejectedElement = new Element();
		rejectedElement.type = ElementType.REJECTED;
	}
	
	// ===============================================================================
	// = FIELDS
	//
	//  - ElementType :  Ps : type
    //  - tag         : KPs : string tag text
    //  - text        : IPs : processed text.  This is not the same as altered.   It may be stored, but not as part of the original.
	//  - owner.id    : iPs : doc or block id.
	
	/**
	 * For now, this will stay a straight enum.  It should take as much memory as a pointer for the platform,
	 * which is pretty much the size as any other aligned type.  If this becomes a problem,
	 * we will have to find a way to pack these in a custom serialized format.
	 */
	public ElementType type;

	/**
	 * Tag text.
	 */
	public String tag;
	
	/**
	 * Term text is true to the original source data.  Element text is processed (not to be confused with altered).
	 */
	public String text;

	/**
	 * Owner system-unique id.
	 */
	public long ownerId;

	
	// ===============================================================================
	// = INTERFACE
	
	public Object clone() throws CloneNotSupportedException {
		super.clone();
		Element result = new Element();
		result.type = type;
		result.tag = tag;
		result.text = text;
		result.ownerId = ownerId;
		return result;
	}

	public int compareTo(Element o) {
		if (o.text == null) {
			if (this.text == null) return 0;
			return 1;
		}
        if (this.text == null) return -1;

		if (this.type == o.type) {
            return typeCompare(this.type, this.text, o.text);

		} else {
			return this.text.compareTo(o.text);
		}
	}

    /**
     * Type compare.  This is exposed so Term can use a consistent compare.
     * @param type
     * @param l
     * @param r
     * @return
     */
	public static int typeCompare(final ElementType type, final String l, final String r) {
        switch(type) {
            case UNKNOWN:
            case REJECTED:
            case STRING:
            case TEXT:
            case BREAKING:
            case TIMESTAMP:
                return l.compareTo(r);

            case LONG:
            case DOUBLE:
                double lnum = Double.parseDouble(l);
                double rnum = Double.parseDouble(r);
                if (lnum > rnum) return 1;
                if (lnum == rnum) return 0;
                return -1;

            case BOOLEAN:
                boolean ltruth = Truth.truth(l);
                boolean rtruth = Truth.truth(r);
                if (ltruth == rtruth) return 0;
                if (ltruth == true) return 1;
                else return -1;

            default:
                throw new Error("BUG BUG BUG!  Unimplemented case for ElementType.  type=" + type.name());
        }
    }

	// ===============================================================================
	// = METHOD
	
	public Element() {
		type = ElementType.UNKNOWN;
	}
	
}
