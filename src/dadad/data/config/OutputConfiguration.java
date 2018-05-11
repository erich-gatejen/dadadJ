package dadad.data.config;

import dadad.platform.config.ConfigurationType;
import dadad.platform.config.validation.ValidationBoolean;

public enum OutputConfiguration implements ConfigurationType {
	
	APPEND_DATA("output.append", "true", "Append output to any existing destination.", new ValidationBoolean());
	
	// == BOILERPLATE - don't touch =================================================================
	private final String property;
    private final String defaultValue;
    private final String help;
    private final Object[] validations;
    private OutputConfiguration(final String property, final String defaultValue, final String help, final Object... validations) {
        this.property = property;
        this.defaultValue = defaultValue;
        this.help = help;
        this.validations = validations;
    }
    // ===============================================================================================

}
