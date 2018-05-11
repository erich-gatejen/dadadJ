package dadad.process.data.wf;

/**
 * Processor instruction
 *
 */
public enum ProcessorInstruction {
	
	/**
	 * Push the current field as String and move to the next field.
	 */
	STRING,
	
	/**
	 * Push the current field as Text and move to the next field.
	 */
	TEXT,
	
	/**
	 * Push as the default field type.
	 */
	FIELD,
	
	/**
	 * Name/value pair.  The name will be the tag.
	 */
	NV;
	
}
