package dadad.data.config;

import dadad.platform.config.ConfigurationType;
import dadad.platform.config.validation.ValidationBoolean;
import dadad.platform.config.validation.ValidationNOP;

public enum ResultConfiguration implements ConfigurationType {
	
	RESULT_POST_OBJECT("result.post.objects", "false", "Post results for each object/block processes.  By default it will only post the final aggregate result.", new ValidationBoolean()),
	RESULT_POST_FILE("result.post.file", null, "Post file for results.  If null, aggregated results will be posted in a log.", new ValidationNOP()),
	RESULT_NAME("result.name", null, "Result name for the report entries.", new ValidationNOP());

	// == BOILERPLATE - don't touch =================================================================
	private final String property;
    private final String defaultValue;
    private final String help;
    private final Object[] validations;
    private ResultConfiguration(final String property, final String defaultValue, final String help, final Object... validations) {
        this.property = property;
        this.defaultValue = defaultValue;
        this.help = help;
        this.validations = validations;
    }
    // ===============================================================================================

}
