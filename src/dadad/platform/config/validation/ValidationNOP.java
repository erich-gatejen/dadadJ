package dadad.platform.config.validation;

public class ValidationNOP extends Validation {
	
	protected String _validationName() {
		return "nop";
	}

	protected void _validate(final Object data) {
		// NOP
	}

}
