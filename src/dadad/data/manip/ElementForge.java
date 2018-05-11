package dadad.data.manip;

import dadad.data.DataContext;
import dadad.data.config.DataConfiguration;
import dadad.data.config.FieldConfiguration;
import dadad.data.model.Element;
import dadad.data.model.ElementType;
import dadad.data.model.Term;

public class ElementForge {
	
	// ===============================================================================
	// = FIELDS
	
	final DataContext context;
	
	final ElementTyper elementTyper;
	final ElementType defaultElementType;
	
	// ===============================================================================
	// = METHODS
	
	public ElementForge(final DataContext context) {
		this.context = context;
		
		boolean doTyping = context.getConfig().getBoolean(DataConfiguration.DO_ELEMENT_TYPING);
		if (doTyping) {
			elementTyper = new ElementTyper(context);
		} else {
			elementTyper = null;
		}
		
		if (context.getConfig().getBoolean(FieldConfiguration.FIELD_DEFAULT_TEXT)) defaultElementType = ElementType.TEXT;
		else defaultElementType = ElementType.STRING;	
	}
	
	public Term set(final Term term) {
		return set(term, null);
	}
	
	public Term set(final Term term, final String tag) {
		term.element = new Element();
		term.element.tag = tag;
		return elementText(term, term.text);	
	}
	
	public Term set(final ElementType type, final Term term) {
		term.element = new Element();
		return elementText(term, type, term.text);
	}
	
	public Term set(final ElementType type, final Term term, final String tag) {
		term.element = new Element();
		term.element.tag = tag;
		return elementText(term, type, term.text);
	}
	
	public Term set(final ElementType type, final String text, final Term term, final String tag) {
		term.element = new Element();
		term.element.tag = tag;
		return elementText(term, type, text);
	}
	
	public Term set(final ElementType type, final String text, final Term term) {
		term.element = new Element();
		return elementText(term, type, text);
	}
		
	public final Term elementText(final Term term, final ElementType type, final String text) {
		if (term.element == null) throw new Error("BUG BUG BUG!  Term does not have an element yet");
		term.element.text = text;
		term.element.type = type;
		return term;
	}
	
	/**
	 * Merges will retain tag if present.
	 * @param term
	 * @return
	 */
	public Term merge(final Term term) {
		String tag = null;
		if (term.element != null) tag = term.element.tag;
		term.element = new Element();
		term.element.tag = tag;
		return elementText(term, term.text);	
	}
	
	public Term merge(final ElementType type, final Term term) {
		String tag = null;
		if (term.element != null) tag = term.element.tag;
		term.element = new Element();
		term.element.tag = tag;
		term.element.type = type;
		term.element.text = term.text;
		return term;
	}
	
	public final Term elementText(final Term term, final String text) {
		if (elementTyper != null) 
			return elementText(term, elementTyper.type(text.trim()), text);
		else
			return elementText(term, defaultElementType, text);	
	}
	
	// ===============================================================================
	// = INTERNAL
	
}
