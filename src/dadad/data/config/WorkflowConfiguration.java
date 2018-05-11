package dadad.data.config;

import dadad.platform.config.ConfigurationType;
import dadad.platform.config.validation.ValidationBoolean;
import dadad.platform.config.validation.ValidationNOP;
import dadad.platform.config.validation.ValidationNotEmpty;
import dadad.platform.config.validation.ValidationPositive;

public enum WorkflowConfiguration implements ConfigurationType {
	
	PROCESSOR("workflow.processor", null, "Name of the element processor processor.", new ValidationNotEmpty()),
	TERMPROCESSOR("workflow.processor.term", null, "Name of the term processor.  Term processors are optional.", new ValidationNOP()),
	
	
	BLOCK_READER("workflow.block.reader", null, "Name of the block reader (use full class name for now).", new ValidationNotEmpty()),
	BLOCK_WRITER("workflow.block.writer", null, "Name of the block writer (use full class name for now).", new ValidationNotEmpty()),
	
	HEADER_IS_PRESENT("workflow.header.is.present", "false", "The first block from the source is a header.", new ValidationNotEmpty()),
	
	PROCESSOR__PROGRAM_FILE("processor.program.file", null, "Program file.", new ValidationNOP()),
	
	NORMALIZE2LOWER("workflow.normalize.lowercase", "true", "Normalize strings to lower case.", new ValidationBoolean()),
	
	ORDER_SCRIPT("order.script", "", "Path to the Order Script file (relative to root).", new ValidationNotEmpty()),
	
	BLOCKS_PER_YEILD("blocks.per.yield", "25", "Number of blocks to process between each system yeild.", new ValidationPositive());
	
	// == BOILERPLATE - don't touch =================================================================
	private final String property;
    private final String defaultValue;
    private final String help;
    private final Object[] validations;
    private WorkflowConfiguration(final String property, final String defaultValue, final String help, final Object... validations) {
        this.property = property;
        this.defaultValue = defaultValue;
        this.help = help;
        this.validations = validations;
    }
    // ===============================================================================================

}
