package dadad.data.model;

import java.io.Serializable;

/**
 * Terms are strings pulled from the raw data.
 */
public class Term implements Serializable, Cloneable, Comparable<Term> {
	
	final static long serialVersionUID = 1;
	
	public final static int POSITION_UNKNOWN = -1;
	
	// ===============================================================================
	// = FIELDS
    //
	//   - text      : iPs : string text.  This may be altered from the original.  Alter may be persisted in the original document.
    //   - start     : iPs : starting offset in block (or document)--inclusive.  Or UNKNOWN (negative number)
    //   - end       : iPs : ending offset in block (or document)--exclusive.  Or UNKNOWN (negative number)
    //   - Element?  : X   : an element - the term processed and decorated


    // Inherent values
	public final String text;	
	public final long start;	// inclusive
	public final long end;	    // exclusive
	
	// Extrapolated values - mutable
	public Element element;
	
	// ===============================================================================
	// = METHODS
	
	public Term(final String text) {
		this(text, POSITION_UNKNOWN, POSITION_UNKNOWN);
	}
	
	public Term(final String text, final long start, final long end) {
		this.text = text;
		this.start = start;
		this.end = end;
	}
	
	public Term alter(final String text) {
		return alter(text, POSITION_UNKNOWN, POSITION_UNKNOWN);
	}
	
	public Term alter(final String text, final long start, final long end) {
		Term term = new Term(text, start, end);
		if (element != null) {
			try {
				term.element = (Element) element.clone();
			} catch (CloneNotSupportedException e) {
				// It is supported.
				throw new Error("This should never happen.", e);
			}
			term.element.text = text;
		}
		term.element = element;
		return term;
	}
	
	public Term set(final ElementType elementType) {
		if (element == null) element = new Element();
		element.type = elementType;
		return this;
	}
	
	public Term set(final ElementType elementType, final String tag) {
		set(elementType);
		element.tag = tag;
		return this;
	}
	
	public Term set(final ElementType elementType, final String tag, final String elementText) {
		set(elementType, tag);
		element.text = text;
		return this;
	}
	
	public boolean isAcceptable() {
		if ((element == null)||(! element.type.isAcceptable())) return false;
		return true;
	}
	
	// ===============================================================================
	// = INTERFACE
	
	public Object clone() throws CloneNotSupportedException {
		Term result = new Term(text, start, end);
		result.element = (Element) element.clone();
		return result;
	}

    /**
     * Compare another term with this one.  If the Element is available for both terms, they will be considered.
     * @param   o the object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *          is less than, equal to, or greater than the specified object.
     */
	public int compareTo(Term o) {

        if ((this.element != null)&&(o.element != null)) {

            String tval = this.text;
            if (this.element.text != null) tval = this.element.text;
            String oval = o.text;
            if (o.element.text != null) oval = o.element.text;

            if (this.element.type == o.element.type) {
                return Element.typeCompare(this.element.type, tval, oval);

            } else {
                return tval.compareTo(oval);
            }

        } else {
            return this.text.compareTo(o.text);
        }
	}
	
}
