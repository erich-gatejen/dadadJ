package dadad.data.model;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import dadad.platform.config.ConfigurationType;

public class Block {
	
	// ===============================================================================
	// = FIELDS
	//
    //  - BLOCKINFO : X   : immutable block info
    //  - raw       : iPs : raw data
    //  - TERM*     :  Ps : block level terms
    //  - Result?   : iPs : Result for the block
    //  - Attrib*   : iPs : Name/Value attributes for the block
	
	// Immutable references
	public final BlockInfo info;
	
	// Mutable
	public String raw;
	private Term[] terms;
	
	// Decorations
	public Result result;	
	private HashMap<String, Object> attributes;
	
	// ===============================================================================
	// = METHODS
	
	public Block(final BlockInfo info, final String raw, Term... terms) {
		this.info = info;
		this.raw = raw;
		this.terms = terms;
	}
	
	public Object getAttrib(final String name) {
		if (attributes == null) return null;
		return attributes.get(name);
	}
	
	public Object getAttrib(final ConfigurationType type) {
		Object result = getAttrib(type.property());
		if (result != null) {
			type.validate(result);
		}
		return result;
	}
	
	/**
	 * Get attributes
	 * @return a set of attributes or null if none are set.
	 */
	public Set<Entry<String, Object>> getAllAttributes() {
		if (attributes == null) return null;
		return attributes.entrySet();
	}
		
	public void setAttrib(final String name, final Object value) {
		if (attributes == null) attributes = new HashMap<String, Object>();
		attributes.put(name, value);
	}
	
	public void setAttrib(final ConfigurationType type, final Object value) {
		setAttrib(type.property(), value);
	}
	
	/***
	 * Get all the terms<br>
	 * <h3>WARNING!  WARNING!  WARNING!  DO NOT ALTER THIS ARRAY!  READ ONLY!</h3>
	 * If you need to change the terms, get a copy.  Please don't make me wrap it.  That would make all the pandas sad.
	 * NOTE: You may change the mutable values in the Term objects.
	 * @return the terms
	 */
	public Term[] getTerms() {
		return terms;
	}
	
	public Term[] copyTerms() {
		Term[] copy = new Term[terms.length];
		for (int index = 0; index < terms.length; index++) {
			try {
				copy[index] = (Term) terms[index].clone();
			} catch (Exception e) {
				throw new Error("BUG BUG BUG!  It should be supported.", e);
			}
		}
		return copy;
	}
	
	public String[] getTermText() {
		String[] copy = new String[terms.length];
		for (int index = 0; index < terms.length; index++) {
			copy[index] = terms[index].text;
		}
		return copy;
	}
	
	public void alterTerms(final boolean raw, final String... termText) {
		if (raw) {
			alterTermsRaw(termText);
			
		} else {
			Term[] newTerms = new Term[terms.length];			
			for (int index = 0; index < terms.length; index++) {
				newTerms[index] = terms[index].alter(termText[index]);
			}
			terms = newTerms;
		}
	}
	
	public void alterTermsRaw(final String... termText) {
		if (termText.length != terms.length) throw new Error("BUG BUG BUG!!!  You cannot alter the number of terms.");
		Term[] newTerms = new Term[terms.length];
		
		// Fix raw
		long frontInterText = 0;
		long endInterText = 0;
		StringBuilder sb = new StringBuilder();
		
		if (terms[0].start > 0) sb.append(raw.substring(0, (int) terms[0].start));
		Term firstTerm = terms[0].alter(termText[0], sb.length(), sb.length() + termText[0].length()); 			
		newTerms[0] = firstTerm;	
		sb.append(termText[0]);
		
		for (int index = 1; index < termText.length; index++) {
			frontInterText = terms[index - 1].end;
			endInterText = terms[index].start;
			if (endInterText > frontInterText) sb.append(raw.substring((int)frontInterText, (int)endInterText));
			
			newTerms[index] = terms[index].alter(termText[index], sb.length(), sb.length() + termText[index].length()); 

			sb.append(termText[index]);		
		}
		
		if (terms[terms.length - 1].end < raw.length()) {
			sb.append(raw.substring((int) terms[terms.length - 1].end));
		}
		
		raw = sb.toString();
		terms = newTerms;		
	}
	
	/**
	 * Alter the terms, not just the term text.
	 * 
	 * There is more I can do here, like reconciling term changes against raw at the same time.  For now, it will assume
	 * the RAW DATA OF THE BLOCK HAS NOT CHANGED within the terms and only the slices applicable to the terms.
	 * 
	 * @param newTerms
	 */
	public void alterTerms(final Term... newTerms) {
		if (terms.length != terms.length) throw new Error("BUG BUG BUG!!!  You cannot alter the number of terms.");
		terms = newTerms;		
	}


}
