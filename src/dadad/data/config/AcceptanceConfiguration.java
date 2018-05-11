package dadad.data.config;

import dadad.platform.config.ConfigurationType;
import dadad.platform.config.validation.ValidationNOP;
import dadad.platform.config.validation.ValidationPositive;

public enum AcceptanceConfiguration implements ConfigurationType {
	
	CHANCE_TO_ACCEPT_BLOCK("ac.chance.to.accept.block", "0", 
			"The chance to accept a block.  If it is 0, it will never accept.  If it 1, it will always accept.  Otherwise" + 
					" it will have a chance expressed as 1 in N, where N is the given number.  It must be a positive integer.",
			new ValidationPositive()),
	
	CHANCE_TO_ACCEPT_ELEMENT("ac.chance.to.accept.element", "0", 
			"The chance to accept an element.  If it is 0, it will never accept.  If it 1, it will always accept.  Otherwise" + 
					" it will have a chance expressed as 1 in N, where N is the given number.  It must be a positive integer.",
			new ValidationPositive()),
	
	ACCEPT_FIRST_NUMBER("ac.accept.first.number", "1", "Automatically accept the first X many.  It must be a positive integer."
			+ "  If you are using a CSV processor and the source has CSV headers in the first line, this must be '1'.",
			new ValidationPositive()),
	
	ACCEPT_BY_POSITION("ac.accept.by.position", null, "Automatically accept elements in the given positions (multivalue).",
			new ValidationNOP());
	
	// == BOILERPLATE - don't touch =================================================================
	private final String property;
    private final String defaultValue;
    private final String help;
    private final Object[] validations;
    private AcceptanceConfiguration(final String property, final String defaultValue, final String help, final Object... validations) {
        this.property = property;
        this.defaultValue = defaultValue;
        this.help = help;
        this.validations = validations;
    }
    // ===============================================================================================

}
