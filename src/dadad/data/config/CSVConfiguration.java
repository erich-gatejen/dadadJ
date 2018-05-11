package dadad.data.config;

import dadad.platform.config.ConfigurationType;
import dadad.platform.config.validation.ValidationNotEmpty;

public enum CSVConfiguration implements ConfigurationType {
	
	HEADER_IS_PRESENT("csv.headerIsPresent", "true", "IF the CSV source has a header line, this should be true.", new ValidationNotEmpty());
	
	// == BOILERPLATE - don't touch =================================================================
	private final String property;
    private final String defaultValue;
    private final String help;
    private final Object[] validations;
    private CSVConfiguration(final String property, final String defaultValue, final String help, final Object... validations) {
        this.property = property;
        this.defaultValue = defaultValue;
        this.help = help;
        this.validations = validations;
    }
    // ===============================================================================================

}
