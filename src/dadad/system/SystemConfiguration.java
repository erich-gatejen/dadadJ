package dadad.system;

import dadad.platform.config.ConfigurationType;
import dadad.platform.config.validation.ValidationNOP;
import dadad.platform.config.validation.ValidationNotEmpty;
import dadad.platform.config.validation.ValidationPositive;

/**
 * System config.
 */
public enum SystemConfiguration implements ConfigurationType {
	
	LOG_DIRECTORY("system.log.dir", "log", "Current log directory.", new ValidationNotEmpty()),
	REST_PORT("system.rest.port", "8585", "Rest api service port.", new ValidationPositive()),
	
	SERVICE_CLASS_LIST("service.class", null, "Numbered classes that will be started as services.  They just implement WorkProcessContainer.", new ValidationNOP()),
	SERVER_API_LIST("server.api", null, "APIs supported by the server.  Name is the API name and the value is the implimenting class.", new ValidationNOP()),
	
	CONTENT_DIRECTORY("server.content.dir", "content", "Path to the content directory relative to install root.");
	
	// == BOILERPLATE - don't touch =================================================================
	private final String property;
    private final String defaultValue;
    private final String help;
    private final Object[] validations;
    private SystemConfiguration(final String property, final String defaultValue, final String help, final Object... validations) {
        this.property = property;
        this.defaultValue = defaultValue;
        this.help = help;
        this.validations = validations;
    }
    // ===============================================================================================

	
}
