package dadad.data.list;

import dadad.data.model.Element;
import dadad.data.model.ElementType;
import dadad.data.model.Term;

import java.io.Serializable;

/**
 * A listed term.
 */
public class ListedTerm implements Comparable<ListedTerm> {

	// ===============================================================================
	// = FIELDS

    public enum Presence {
        NONE,
        ONLY_ONE,
        AT_LEAST_ONE;
    }


	Term term;
    Presence presence;
    int number;



	// ===============================================================================
	// = METHODS

    public ListedTerm(final Term term) {
        this.term = term;
    }



	// ===============================================================================
	// = INTERFACE

    /**
     * Compare another term with this one.  If the Element is available for both terms, they will be considered.
     * @param   o the object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *          is less than, equal to, or greater than the specified object.
     */
	public int compareTo(ListedTerm o) {
        return this.term.compareTo(o.term);
	}
	
}
