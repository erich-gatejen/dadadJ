package dadad.platform.config.validation;

import dadad.platform.Truth;

public class ValidationBoolean extends Validation {
	
	protected String _validationName() {
		return "determine truth (boolean)";
	}

	protected void _validate(final Object data) {
		new Truth(data);	// It will throw an exception if it cannot be determined.
	}

}
