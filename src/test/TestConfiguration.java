package test;

import dadad.platform.config.ConfigurationType;
import dadad.platform.config.validation.ValidationNOP;
import dadad.platform.config.validation.ValidationNotEmpty;
import dadad.platform.config.validation.ValidationWritableFilePath;

public enum TestConfiguration implements ConfigurationType {

    TEST_ROOT("test.root", "test.dadad", "Root package for test discovery.", new ValidationNOP()),
    TEST_RESULT_FILE("test.result.file", null, "Path to a file to dump results.  If this is not set, it will go to stdout.", new ValidationWritableFilePath()),
    TEST_RESULT_OUTPUT_TYPE("test.result.type", TestOutputType.TEXT.name(), "Output type for the result.", TestOutputType.class),
    TEST_ERROR_FILE("test.result.error.file", null, "Path to a file to dump error-only results.  If this is not set, it will go to stdout.", new ValidationWritableFilePath()),
    TEST_RESULT_ERROR_TYPE("test.result.error.type", TestOutputType.NONE.name(), "Output type for the errors.  If set to NONE it will not be output", TestOutputType.class);


	// == BOILERPLATE - don't touch =================================================================
	private final String property;
    private final String defaultValue;
    private final String help;
    private final Object[] validations;
    private TestConfiguration(final String property, final String defaultValue, final String help, final Object... validations) {
        this.property = property;
        this.defaultValue = defaultValue;
        this.help = help;
        this.validations = validations;
    }
    // ===============================================================================================

}
