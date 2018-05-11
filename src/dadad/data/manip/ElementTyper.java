package dadad.data.manip;

import dadad.data.DataContext;
import dadad.data.model.ElementType;
import dadad.platform.StringChewer;

public class ElementTyper {
	
	// ===============================================================================
	// = FIELDS
	
	final DataContext context;
	
	// ===============================================================================
	// = METHODS
	
	public ElementTyper(final DataContext context) {
		this.context = context;
	}
	
	public ElementType type(final String data) {
		if (data == null) throw new Error("BUG BUG BUG!  Null element data.");
		StringChewer reader = new StringChewer(data);
		return _OPEN(reader);
	}
	
	// ===============================================================================
	// = INTERNAL
	
	private ElementType _OPEN(final StringChewer chew) {
		if (! chew.hasMore()) return ElementType.TEXT;
		switch(chew.next()) {
		case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': case '0': case '+': case '-': 
			return _NUMERIC(chew);
			
		case '.':
			if (! chew.hasMore()) return ElementType.TEXT;
			return _NUMERIC_PERIOD(chew);
	
		default:
			return ElementType.TEXT;
		}		
	}
	
	private ElementType _NUMERIC(final StringChewer chew) {
		while(chew.hasMore()) {
			switch(chew.next()) {
			case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': case '0': 
				break;
			
			case '.':
				return _NUMERIC_PERIOD(chew);
		
			default:
				return ElementType.TEXT;
			}		
		}
		return ElementType.LONG;
	}
	
	private ElementType _NUMERIC_PERIOD(final StringChewer chew) {
		while(chew.hasMore()) {
			switch(chew.next()) {
			case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': case '0': 
				break;
		
			default:
				return ElementType.TEXT;
			}	
		}
		return ElementType.DOUBLE;
	}
	
	
}
