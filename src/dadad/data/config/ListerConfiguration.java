package dadad.data.config;

import dadad.data.list.ListerCatalog;
import dadad.platform.config.ConfigurationType;
import dadad.platform.config.validation.ValidationNotEmpty;
import dadad.platform.config.validation.ValidationWritableFilePath;

public enum ListerConfiguration implements ConfigurationType {
	
	LIST_TYPE("lister.list.type", "", "IF the CSV source has a header line, this should be true.", ListerCatalog.class),
	LIST_FILE("lister.list.file", null, "Path to the list file", new ValidationNotEmpty(), new ValidationWritableFilePath());
	
	// == BOILERPLATE - don't touch =================================================================
	private final String property;
    private final String defaultValue;
    private final String help;
    private final Object[] validations;
    private ListerConfiguration(final String property, final String defaultValue, final String help, final Object... validations) {
        this.property = property;
        this.defaultValue = defaultValue;
        this.help = help;
        this.validations = validations;
    }
    // ===============================================================================================

}
