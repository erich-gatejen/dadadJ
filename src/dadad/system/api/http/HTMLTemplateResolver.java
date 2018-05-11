package dadad.system.api.http;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import dadad.platform.AnnotatedException;

public class HTMLTemplateResolver {

	// ===============================================================================
	// = FIELDS

	public final static char FLAG_START = '$';
	public final static char FLAG_OPEN_TEMPLATE = '[';	
	public final static char FLAG_CLOSE_TEMPLATE = ']';	
	public final static char FLAG_TOKEN_SEP = ',';	
	public final static char FLAG_ESCAPE = '\\';	
	
	public enum Actions {
		SCRIPTCSS("Add defined script and css sections to the given spot.  If you use any of the ADD* commands, "
				+ "be sure to add this to the page header.",
				new String[] {}),
		
		ADDACTION("Add an action.  Typically you would define as an onClick attribute.  It will call a single API.",
				new String[] {
				"Action name: Should be unique per page.",
				"Form id: The same as the id attribute for the containing form.",
				"URL: It should be a URL to the API call invoked",
				"Result target: The name of the page variable where the API call result should be put.",
				"errorPage: Error landing page if there is an error.  If blank, it will be the same page.",
				"N/V pairs (optional): Name/Value pairs that will be sent as parameters to the API.  The name is the API parameters name"
					+ " and the value is the name of the page value to use as the parameter value."}),
		
		ADDHELP("Add the help for a specific API.",
				new String[] { "Api name: the api name as define in the server.api.* configuration properties.  For example: 'SERVER'" }),
		
		
		APILIST("Get the list of available APIs.",
				new String[] {} ),
		
		ADDRESULTTARGET("Add a result target as the given page variable",
				new String[] { "Name of the result target as specified in the Action." }),
				
				
		ADDREPORT("Add special report section.  This inclues the " + HttpServer.SPECIAL_RESULT_TARGET_PARAM + " target named ___success.report, which can be used by ADDACTION as the target.",
				new String[] {} ),
		
		ADDPROP("Add property to a form.  It will be hidden", 
				new String[] {"Property name", "Property value"} );
		
		private final String help;
		private final String[] parameters;
		private Actions(final String help, final String[] parameters) {
			this.help = help;
			this.parameters = parameters;
		}
		
		public String getHelp() {
			return help;
		}
	
		public String[] getParameters() {
			return parameters;
		}
	}

	private final HTMLTemplateResolverHandler handler;
	
	/**
	 * HAX: some of the action cause embedded HTML
	 */

	private enum SectionDo { 
		ADD_SCRIPT_AND_CSS;
	}

	
	// ===============================================================================
	// = METHOD

	public HTMLTemplateResolver(final HTMLTemplateResolverHandler handler) {
		this.handler = handler;
	}
	
	public String resolve(final String text) {
		return resolve(new StringReader(text));
	}
	
	public Reader currentReader;
	public StringBuilder currentBuilder;
	public LinkedList<Object> sectionList;
	public LinkedList<String> tokenList;
	public StringBuilder tokenErrorBuilder;
	
	public synchronized String resolve(final Reader reader) {
		currentReader = reader;
		currentBuilder = new StringBuilder();
		sectionList = new LinkedList<Object>();
		start();
		
		return compileList();
	}
	
	public synchronized void start() {
	
		try {
		
			int character = currentReader.read();
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
				
				character = currentReader.read();
			}
		
		} catch (IOException e) {
			throw new AnnotatedException("Failed reading while resolving source.");
		}
		
		if (currentBuilder.length() > 0) sectionList.add(currentBuilder.toString());	
	
	}
	
	private void ESCAPE() throws IOException {
		int character = currentReader.read();
		if (character < 0) throw new AnnotatedException("Dangling escape character.");
		currentBuilder.append((char) character);
		if (tokenErrorBuilder != null) tokenErrorBuilder.append((char) character);
	}
	
	private enum State {
		ENTER,
		OPEN_TEMPLATE1,
		READ_TEMPLATE,
		CLOSE_TEMPLATE1;
	}
	
	private void START() throws IOException {
		
		int character = currentReader.read();
		State state = State.ENTER;
		while (character >= 0) {
		
			switch(state) {
			case ENTER:
				switch(character) {
					
				case FLAG_OPEN_TEMPLATE:	
					state = State.OPEN_TEMPLATE1;
					break;
					
				case FLAG_ESCAPE:
					currentBuilder.append(FLAG_START);
					ESCAPE();
					return;

				case FLAG_START:
				case FLAG_CLOSE_TEMPLATE:									
				default:
					currentBuilder.append(FLAG_START);
					currentBuilder.append((char) character);
					return;				
				}	
				break;
				
			case OPEN_TEMPLATE1:
				switch(character) {
					
				case FLAG_OPEN_TEMPLATE:	
					state = State.READ_TEMPLATE;
					sectionList.add(currentBuilder.toString());
					tokenList = new LinkedList<String>();
					tokenErrorBuilder = new StringBuilder();
					currentBuilder = new StringBuilder();
					break;
					
				case FLAG_ESCAPE:
					currentBuilder.append(FLAG_START);
					currentBuilder.append(FLAG_OPEN_TEMPLATE);
					ESCAPE();
					return;
					
				default:
					currentBuilder.append(FLAG_START);
					currentBuilder.append(FLAG_OPEN_TEMPLATE);
					currentBuilder.append((char) character);
					return;					
				}
				break;
				
			case READ_TEMPLATE:
				switch(character) {
				
				case FLAG_CLOSE_TEMPLATE:
					state = State.CLOSE_TEMPLATE1;
					break;
					
				case FLAG_ESCAPE:
					ESCAPE();
					break;
					
				case FLAG_TOKEN_SEP:
					tokenList.add(currentBuilder.toString());
					currentBuilder = new StringBuilder();
					tokenErrorBuilder.append((char) character);
					break;
				
				default:
					currentBuilder.append((char) character);
					tokenErrorBuilder.append((char) character);
					break;
				}
				break;
				
			case CLOSE_TEMPLATE1:
				if (character == FLAG_CLOSE_TEMPLATE) {
					state = State.ENTER;
					tokenList.add(currentBuilder.toString());					
					sectionList.add(action());
					currentBuilder = new StringBuilder();
					tokenErrorBuilder = null;
					return;
					
				} if (character == FLAG_TOKEN_SEP) {
					currentBuilder.append((char) FLAG_CLOSE_TEMPLATE);
					tokenErrorBuilder.append((char) FLAG_CLOSE_TEMPLATE);
					tokenErrorBuilder.append((char) character);
					tokenList.add(currentBuilder.toString());
					currentBuilder = new StringBuilder();	
					state = State.READ_TEMPLATE;

				} else {
					currentBuilder.append((char) character);
					tokenErrorBuilder.append((char) character);
					state = State.READ_TEMPLATE;
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
			
		case OPEN_TEMPLATE1:
			currentBuilder.append(FLAG_START);
			currentBuilder.append(FLAG_OPEN_TEMPLATE);
			return;
			
		case READ_TEMPLATE:
			throw new AnnotatedException("Dangling variable definition.");
			
		case CLOSE_TEMPLATE1:
			throw new AnnotatedException("Dangling variable definition (broken close).");
		}
	}
	
	private Object action() {
		Object result = "";
		
		if (tokenList.isEmpty()) return result;  // Do nothing.
		
		Actions action;
		String actionText = tokenList.removeFirst().toUpperCase();
		try {
			action = Actions.valueOf(actionText);
		} catch (Exception ee) {
			throw new AnnotatedException("Unknown template action").annotate("template.action", actionText);
		}
		
		switch(action) {

		case SCRIPTCSS:
			result = SectionDo.ADD_SCRIPT_AND_CSS;
			break;
			
		case ADDRESULTTARGET:
			try {
				result = "$((" + tokenList.removeFirst() + "))";

			} catch (NoSuchElementException nsee) {
				throw new AnnotatedException("Not enough parameters for " + Actions.ADDRESULTTARGET.name() + " action.")
					.annotate("value", tokenErrorBuilder.toString());
			}
			break;
			
		case APILIST:
			result = handler.addApiList();
			break;
			
		case ADDHELP:	
			try {
				result = handler.addHelp(tokenList.removeFirst());
				result = reResolve(result.toString());

			} catch (NoSuchElementException nsee) {
				throw new AnnotatedException("Not enough parameters for " + Actions.ADDHELP.name() + " action.")
					.annotate("value", tokenErrorBuilder.toString());
			}
			break;
	
		case ADDACTION:					
			try {
							
				String actionName = tokenList.removeFirst();
				String formId = tokenList.removeFirst();
				String apiUrl = tokenList.removeFirst();
				String resultTarget = tokenList.removeFirst();
				String errorPage = tokenList.removeFirst();
				String[] params = null;
				if (tokenList.size() > 0) {
					params = tokenList.toArray(new String[tokenList.size()]);
				}
				
				result = handler.addAction(actionName, formId, apiUrl, resultTarget, errorPage, params);

			} catch (NoSuchElementException nsee) {
				throw new AnnotatedException("Not enough parameters for " + Actions.ADDACTION.name() + " action.")
					.annotate("value", tokenErrorBuilder.toString());
			}	
			break;

		case ADDREPORT:
			result = "<pre>$((" + HttpServer.SPECIAL_ERROR_REPORT_PARAM + "))</pre>$((___success.report))";
			break;
			
		case ADDPROP:
			String propName = tokenList.removeFirst();
			String propValue = tokenList.removeFirst();
			result = "<input type=\"hidden\" name=\"" + propName + "\" value=\"" + propValue + "\">";
			break;
						
		} 
		
		return result;
	}
	
	private String reResolve(final String string) {
		
		Reader saveCurrentReader = currentReader;
		StringBuilder saveCurrentBuilder = currentBuilder;
		LinkedList<Object> saveSectionList = sectionList;
		LinkedList<String> saveTokenList = tokenList;
		StringBuilder saveTokenErrorBuilder = tokenErrorBuilder;
		
		currentReader = new StringReader(string);
		currentBuilder = new StringBuilder();
		sectionList = new LinkedList<Object>();
		
		start();
		String result = compileList();
		
		currentReader = saveCurrentReader;
		currentBuilder = saveCurrentBuilder;
		sectionList = saveSectionList;
		tokenList = saveTokenList;
		tokenErrorBuilder = saveTokenErrorBuilder;
		
		return result;
	}
	
	private String compileList() {
		StringBuilder sb = new StringBuilder();
		
		for (Object item : sectionList) {
			
			if (item instanceof String) {
				sb.append((Object) item);
				
			} else if (item instanceof SectionDo) {
				switch((SectionDo) item) {
				
				case ADD_SCRIPT_AND_CSS:
					sb.append(handler.getScript() + "\r\n" + handler.getCss() + "\r\n");
					break;
					
				default:
					throw new Error("BUG BUG BUG!  Unknown SectionDo made it into the sectionList.  value=" + ((SectionDo) item).name());
				}
				
			} else {
				throw new Error("BUG BUG BUG!  Unknown object made it into the sectionList.  class=" + item.getClass().getName());
				
			}
			
		}
				
		return sb.toString();
	}
	    
}

