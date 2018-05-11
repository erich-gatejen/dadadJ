package dadad.platform.config;

import dadad.platform.PlatformDataType;
import dadad.platform.config.validation.ValidationNotEmpty;
import dadad.platform.config.validation.ValidationReadableFilePath;
import dadad.platform.services.LoggerLevel;

/**
 * Context config.
 *
 * These look a lot better in dlang.
 */
public enum ContextConfiguration implements ConfigurationType {
	
	CONTEXT_COMMAND("command", null, "Command being executed.", new ValidationNotEmpty()),

	CONTEXT_SOURCE("source", null, "Path to source file or directory with sources.", new ValidationNotEmpty()),
	CONTEXT_TARGET("target", null, "Path to target file or directory for reporting.", new ValidationNotEmpty()),
	CONTEXT_RUN("run", null, "Run name (uniquifying token).", new ValidationNotEmpty()),
	
	CONTEXT_DATATYPE("data.type", null, "Current datatype in use by platform (as opposed to the datatype being processed).", PlatformDataType.class),
	
	CONTEXT_ROOT_PATH("root.path", null, "Root filesystem path to there context space.", new ValidationNotEmpty()),
	
	CONTEXT_SHARE_PATH("share.path", null, "Shared filesystem path.  Put files here that need to be shared.", new ValidationNotEmpty()),
	
	CONTEXT_WORKFLOW_CLASS("workflow.class", null, "Workflow class.", new ValidationNotEmpty()),
	
	CONTEXT_LOGLEVEL("log.level", LoggerLevel.INFO.name(), "Logger level", LoggerLevel.class),

	CONTEXT_PROP_FILE("prop.file", null, "Path to a property file", ValidationReadableFilePath.class);
	
	// == BOILERPLATE - don't touch =================================================================
	private final String property;
    private final String defaultValue;
    private final String help;
    private final Object[] validations;
    private ContextConfiguration(final String property, final String defaultValue, final String help, final Object... validations) {
        this.property = property;
        this.defaultValue = defaultValue;
        this.help = help;
        this.validations = validations;
    }
    // ===============================================================================================

	
}
