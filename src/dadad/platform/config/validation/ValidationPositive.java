package dadad.platform.config.validation;

public class ValidationPositive extends Validation {
	
	protected String _validationName() {
		return "numeric is a positive";
	}

	protected void _validate(final Object data) {

		if (data == null) throw new RuntimeException("Data is null");
		long value = -1;
		
		// Only String can be empty
		if ((data instanceof Integer) || (data instanceof Long)) {
			value = (long) data;
			
		} else if (data instanceof String) {
			if ( ((String)data).trim().length() < 1) throw new RuntimeException("Value is empty.");
			try {
				value = Long.parseLong((String) data);
				
			} catch (NumberFormatException nfe) {
				throw new RuntimeException("String data is not a valid numeric.");
			}
			
		} else {
			throw new RuntimeException("Value cannot be used as a numeric.");
		}
		
		if (value < 0) throw new RuntimeException("Not a positive.");			
	
	}

}
