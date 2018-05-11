package dadad.data.config;

import dadad.platform.config.ConfigurationType;
import dadad.platform.config.validation.ValidationBoolean;

public enum StoreIndexConfiguration implements ConfigurationType {

	ELEMENT__INDEX_OWNER_ID("element.index.owner.id", "true", "Index the owner id (block or document) with", new ValidationBoolean());

	// == BOILERPLATE - don't touch =================================================================
	private final String property;
    private final String defaultValue;
    private final String help;
    private final Object[] validations;
    private StoreIndexConfiguration(final String property, final String defaultValue, final String help, final Object... validations) {
        this.property = property;
        this.defaultValue = defaultValue;
        this.help = help;
        this.validations = validations;
    }
    // ===============================================================================================

}
