package dadad.data.io;

import java.util.ArrayList;

import dadad.platform.AnnotatedException;

public class CSVReader {

	// ===============================================================================
	// = FIELDS
	
	
	// ===============================================================================
	// = METHODS
	
	enum State {
		OPEN,
		QUOTE,
		QUOTED,
		QUOTE_IN_QUOTE;	
	}
	
	public static String[] split(String value) {
		
		StringBuilder accumulator = new StringBuilder();
		ArrayList<String> items = new ArrayList<String>();
		State state = State.OPEN;
		char current;
	
		for (int index = 0; index < value.length(); index++) {
			current = value.charAt(index);
			
			switch(state) {
			
			case OPEN:
				switch(current) {
				case '"':
					state = State.QUOTE;
					break;
					
				case ',':
					if (accumulator.length() > 0) {
						items.add(accumulator.toString());
						accumulator = new StringBuilder();
					}
					break;
					
				default:
					accumulator.append(current);
					break;						
				}
				break;
				
			case QUOTE:
				switch(current) {
				case '"':
					accumulator.append('"');
					state = State.OPEN;					
					break;						
					
				// case ',':				
				default:
					accumulator.append(current);
					state = State.QUOTED;
				}
				break;
				
			case QUOTED:				
				switch(current) {
				case '"':
					state = State.QUOTE_IN_QUOTE;
					break;
					
				// case ',':					
				default:
					accumulator.append(current);
					break;						
				}
				break;				
			
			case QUOTE_IN_QUOTE:				
				switch(current) {
				case '"':
					accumulator.append('"');
					state = State.QUOTED;
					break;						
					
				case ',':
					items.add(accumulator.toString());
					accumulator = new StringBuilder();
					state = State.OPEN;
					break;
					
				default:
					accumulator.append(current);
					break;													
				}
				break;
				
			}
		
		}
		
		// dangles
		if (state == State.QUOTE_IN_QUOTE) {
			items.add(accumulator.toString());
		} else if (state != State.OPEN) {
			throw new AnnotatedException("Dangling CSV field.").annotate("value", value);			
		} else if (accumulator.length() > 0) {
			items.add(accumulator.toString());
		}

		String[] itemsArray = null;
		if (items.size() > 0) 
			itemsArray = items.toArray(new String[items.size()]);
		return itemsArray;		
	}
	
	// ===============================================================================
	// = ABSTRACT


}
