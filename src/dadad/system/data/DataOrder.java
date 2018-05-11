package dadad.system.data;

import dadad.platform.AnnotatedException;

/**
 * Orders.
 */
public enum DataOrder {	
	
	// STEP	[tab] name [tab] classname [tab] type
	// TEST [tab] test/group class {[tab] fail forward}
	// PROCESSOR [tab] name
	// START [tab] name
	// HEADER [tab] name
	// PROCESS	[tab] name {[tab] fail forward}
	// END [tab] name
	// TEMP [tab] prop [tab] suffix	
	// SHARE [tab] prop [tab] suffix	
	// COPY [tab] source.prop [tab] dest.prop
	// FORWARD  [tab] fail forward				- Set the default fail forward destination
	
	STEP(new String[]{ "STEP", "name", "classname", "type (OBJECT|BLOCK)" }),
	TEST(new String[]{ "TEST", "test/group class"}),
	PROCESSOR(new String[]{ "PROCESSOR", "name" }),
	TERMPROCESSOR(new String[]{"TERMPROCESSOR", "name"}),
	START(new String[]{ "START", "name" }),
	HEADER(new String[]{ "HEADER", "name" }),
	PROCESS(new String[]{ "PROCESS", "name" }),
	END(new String[]{ "END", "name" }),
	TEMP(new String[]{ "TEMP", "property name (to get path to temp file)", "temp file suffix" }),
	SHARE(new String[]{ "SHARE", "property name (to get path to share temp file)", "share temp file suffix" }),
	COPY(new String[]{ "COPY", "source property", "destination property" }),
	FORWARD(new String[]{ "FORWARD", "fail forward destination" });

	private final String[] parameters;
	private DataOrder(final String[] parameters) {
		this.parameters = parameters;
	}
	
	public String[] getParameters() {
		return parameters;
	}
	
	public void validate(final String[] tokens) {
		if (tokens.length < parameters.length) throw new AnnotatedException("Not enough parameters").annotate("order", name());
		for (int index = 0; index < tokens.length; index++) {
			if (tokens[index].trim().length() < 1)  throw new AnnotatedException("Parameter is empty.")
				.annotate("order", name(), "parameter", parameters[index]);
		}
	}

}
