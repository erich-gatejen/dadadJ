package dadad.data.config;

import dadad.platform.config.ConfigurationType;
import dadad.platform.config.validation.ValidationBoolean;

public enum FieldConfiguration implements ConfigurationType {
	
	FIELD_DEFAULT_TEXT("field.default.text", "true", 
			"If true (default), the fields will be treated as a Text and will be tokanized.  Otherwise, " +
				"the field will not be tokanized for indexing.",
			new ValidationBoolean());
	
	// == BOILERPLATE - don't touch =================================================================
	private final String property;
    private final String defaultValue;
    private final String help;
    private final Object[] validations;
    private FieldConfiguration(final String property, final String defaultValue, final String help, final Object... validations) {
        this.property = property;
        this.defaultValue = defaultValue;
        this.help = help;
        this.validations = validations;
    }
    // ===============================================================================================

}
