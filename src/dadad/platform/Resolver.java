package dadad.platform;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class Resolver {

	// ===============================================================================
	// = FIELDS

	public final static char FLAG_START = '$';
	public final static char FLAG_OPEN_CONFIG = '{';	
	public final static char FLAG_OPEN_VAR = '(';	
	public final static char FLAG_OPEN_INCLUDE = '[';
	public final static char FLAG_CLOSE_CONFIG = '}';	
	public final static char FLAG_CLOSE_VAR = ')';	
	public final static char FLAG_CLOSE_INCLUDE = ']';	
	public final static char FLAG_ESCAPE = '\\';	
	
	private final ResolverHandler handler;

	// ===============================================================================
	// = METHOD

	public Resolver(final ResolverHandler handler) {
		this.handler = handler;
	}
	
	public String resolve(final String text) {
		return resolve(new StringReader(text));
	}
	
	public Reader currentReader;
	public StringBuilder currentBuilder;
	
	public synchronized String resolve(final Reader reader) {
		currentReader = reader;
		currentBuilder = new StringBuilder();
	
		try {
		
		int character = reader.read();
		while (character >= 0) {
			
			switch(character) {
			case FLAG_START:
				START();
				break;
				
			case FLAG_ESCAPE:
				ESCAPE();
				break;
				
			default:
				currentBuilder.append((char) character);
				break;
			}
			
			character = reader.read();
		}
		
		} catch (IOException e) {
			throw new AnnotatedException("Failed reading while resolving source.");
		}
		
		return currentBuilder.toString();	
	}
	
	private void ESCAPE() throws IOException {
		int character = currentReader.read();
		if (character < 0) throw new AnnotatedException("Dangling escape character.");
		currentBuilder.append((char) character);
	}
	
	private enum State {
		ENTER,
		OPEN_VAR1,
		READ_VAR,
		CLOSE_VAR1,
		OPEN_CONFIG1,
		READ_CONFIG,
		CLOSE_CONFIG1,
		OPEN_INCLUDE1,
		READ_INCLUDE,
		CLOSE_INCLUDE1;
	}
	
	private void START() throws IOException {
		StringBuilder savedStringBuilder = null;
		
		int character = currentReader.read();
		State state = State.ENTER;
		while (character >= 0) {
		
			switch(state) {
			case ENTER:
				switch(character) {
					
				case FLAG_OPEN_VAR:	
					state = State.OPEN_VAR1;
					break;
					
				case FLAG_OPEN_CONFIG:	
					state = State.OPEN_CONFIG1;
					break;
					
				case FLAG_OPEN_INCLUDE:	
					state = State.OPEN_INCLUDE1;
					break;
					
				case FLAG_ESCAPE:
					currentBuilder.append(FLAG_START);
					ESCAPE();
					return;

				case FLAG_START:
				case FLAG_CLOSE_VAR:	
				case FLAG_CLOSE_CONFIG:
				case FLAG_CLOSE_INCLUDE:								
				default:
					currentBuilder.append(FLAG_START);
					currentBuilder.append((char) character);
					return;				
				}	
				break;
				
			case OPEN_VAR1:
				switch(character) {
					
				case FLAG_OPEN_VAR:	
					state = State.READ_VAR;
					savedStringBuilder = currentBuilder;
					currentBuilder = new StringBuilder();
					break;
					
				case FLAG_ESCAPE:
					currentBuilder.append(FLAG_START);
					currentBuilder.append(FLAG_OPEN_VAR);
					ESCAPE();
					return;
					
				default:
					currentBuilder.append(FLAG_START);
					currentBuilder.append(FLAG_OPEN_VAR);
					currentBuilder.append((char) character);
					return;					
				}
				break;
				
			case READ_VAR:
				if (character == FLAG_CLOSE_VAR) {
					state = State.CLOSE_VAR1;
				} else {
					currentBuilder.append((char) character);					
				}
				break;
				
			case CLOSE_VAR1:
				if (character == FLAG_CLOSE_VAR) {
					state = State.ENTER;
					savedStringBuilder.append(handler.resolveVariable(currentBuilder.toString()));
					currentBuilder = savedStringBuilder;
					return;
					
				} else {
					currentBuilder.append((char) character);
					state = State.READ_VAR;
				}
				break;
				
				
			case OPEN_CONFIG1:
				switch(character) {
					
				case FLAG_OPEN_CONFIG:	
					state = State.READ_CONFIG;
					savedStringBuilder = currentBuilder;
					currentBuilder = new StringBuilder();
					break;
					
				case FLAG_ESCAPE:
					currentBuilder.append(FLAG_START);
					currentBuilder.append(FLAG_OPEN_CONFIG);
					ESCAPE();
					return;
					
				default:
					currentBuilder.append(FLAG_START);
					currentBuilder.append(FLAG_OPEN_VAR);
					currentBuilder.append((char) character);
					return;					
				}
				break;
				
			case READ_CONFIG:
				if (character == FLAG_CLOSE_CONFIG) {
					state = State.CLOSE_CONFIG1;
				} else {
					currentBuilder.append((char) character);					
				}
				break;
				
			case CLOSE_CONFIG1:
				if (character == FLAG_CLOSE_CONFIG) {
					state = State.ENTER;
					savedStringBuilder.append(handler.resolveConfiguration(currentBuilder.toString()));
					currentBuilder = savedStringBuilder;
					return;
					
				} else {
					currentBuilder.append((char) character);
					state = State.READ_CONFIG;
				}
				break;
			
			case OPEN_INCLUDE1:
				switch(character) {
				
				case FLAG_OPEN_INCLUDE:				
					state = State.READ_INCLUDE;
					savedStringBuilder = currentBuilder;
					currentBuilder = new StringBuilder();
					break;
					
				case FLAG_ESCAPE:
					currentBuilder.append(FLAG_START);
					currentBuilder.append(FLAG_OPEN_INCLUDE);
					ESCAPE();
					return;
					
				default:
					currentBuilder.append(FLAG_START);
					currentBuilder.append(FLAG_OPEN_INCLUDE);
					currentBuilder.append((char) character);
					return;					
				}
				break;
				
			case READ_INCLUDE:
				if (character == FLAG_CLOSE_INCLUDE) {
					state = State.CLOSE_INCLUDE1;
				} else {
					currentBuilder.append((char) character);					
				}
				break;
				
			case CLOSE_INCLUDE1:
				if (character == FLAG_CLOSE_INCLUDE) {
					state = State.ENTER;
					handler.resolveInclude(currentBuilder.toString());
					currentBuilder = savedStringBuilder;
					return;
					
				} else {
					currentBuilder.append((char) character);
					state = State.READ_INCLUDE;
				}
				break;
				
			}
			
			character = currentReader.read();
		}
		
		// Error
		switch(state) {
		case ENTER:
			// This shouldn't be possible?
			return;
			
		case OPEN_VAR1:
			currentBuilder.append(FLAG_START);
			currentBuilder.append(FLAG_OPEN_VAR);
			return;
			
		case READ_VAR:
			throw new AnnotatedException("Dangling variable definition.");
			
		case CLOSE_VAR1:
			throw new AnnotatedException("Dangling variable definition (broken close).");

		case OPEN_CONFIG1:
			currentBuilder.append(FLAG_START);
			currentBuilder.append(FLAG_OPEN_CONFIG);
			return;
			
		case READ_CONFIG:
			throw new AnnotatedException("Dangling configuration definition.");
			
		case CLOSE_CONFIG1:
			throw new AnnotatedException("Dangling configuration definition (broken close).");

			
		case OPEN_INCLUDE1:
			currentBuilder.append(FLAG_START);
			currentBuilder.append(FLAG_OPEN_INCLUDE);
			return;
			
		case READ_INCLUDE:
			throw new AnnotatedException("Dangling include definition.");
			
		case CLOSE_INCLUDE1:
			throw new AnnotatedException("Dangling include definition (broken close).");
		}
	}
	
	    
}

