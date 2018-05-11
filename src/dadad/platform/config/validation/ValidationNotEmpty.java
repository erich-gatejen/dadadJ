package dadad.platform.config.validation;

public class ValidationNotEmpty extends Validation {
	
	protected String _validationName() {
		return "data is not empty";
	}

	protected void _validate(final Object data) {

		if (data == null) throw new RuntimeException("Data is null");
		
		// Only String can be empty
		if (data instanceof String) {
			if ( ((String)data).trim().length() < 1) throw new RuntimeException("Value is empty.");
		}

	}

}
