package dadad.data.config;

import dadad.data.DataType;
import dadad.platform.config.ConfigurationType;
import dadad.platform.config.validation.ValidationBoolean;
import dadad.platform.config.validation.ValidationNotEmpty;

public enum DataConfiguration implements ConfigurationType {
	
	TYPE("data.type", null, "Data type, such as 'csv' or 'log'.", DataType.class),
	DO_ELEMENT_TYPING("data.do.element.typing", "true", "Do typing when creating Elements", new ValidationNotEmpty()),
	
	ALTER_TERMS_POSITION("alter.element.by.position", "0", "Multivalue positions  of elements to alter.  Starts at position 0", new ValidationNotEmpty()),
	ALTER_RAW("alter.raw", "true", "If altering elements and this is true, the original raw data will be altered to match.  Note that " +
			"all terms that match the altered term will also be altered.", new ValidationBoolean()),
	
	
	// Block metadata attributes
	META__IS_HEADER("___meta___.is.header", "false", null, ValidationBoolean.class);
	
	// == BOILERPLATE - don't touch =================================================================
	private final String property;
    private final String defaultValue;
    private final String help;
    private final Object[] validations;
    private DataConfiguration(final String property, final String defaultValue, final String help, final Object... validations) {
        this.property = property;
        this.defaultValue = defaultValue;
        this.help = help;
        this.validations = validations;
    }
    // ===============================================================================================

}
