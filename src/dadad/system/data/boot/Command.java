package dadad.system.data.boot;

import dadad.platform.AnnotatedException;

public enum Command {
	ANALYSIS,
	REPORT,
	BOTH,
	LIST,
	WF;
	
	public static Command match(final String commandText) {
		Command result;
		
		if (commandText == null) throw new Error("BUG BUG BUG!!!  Command text is a null.");
		try {
			result = Command.valueOf(commandText.trim().toUpperCase());
			
		} catch (Exception e) {
			throw new AnnotatedException("Unknown command").annotate("command.text", commandText);
			
		}
	
		return result;
	}
	
}
