package dadad.platform.config.validation;

import dadad.platform.AnnotatedException;

public abstract class Validation {

	// ===============================================================================
	// = ABSTRACT
		
	/**
	 * Implementation of the validation.  Throw an exception if the validation fails.
	 * @param data
	 */
	protected abstract void _validate(final Object data);
	
	protected abstract String _validationName();
		
	// ===============================================================================
	// = METHODS
	
	/**
	 * Validate the data.
	 * @param data
	 * @param errorMessage
	 * @param throwExceptionIfFail
	 * @return true if the data passes validation, otherwise false.
	 */
	public boolean validate(final Object data, final String errorMessage, final boolean throwExceptionIfFail) {
		
		boolean result = false;
		try {
			_validate(data);
			result = true;
						
		} catch (Exception e) {
			if (throwExceptionIfFail) {
				if (data == null)
					throw new AnnotatedException(errorMessage, e).annotate("validation.name", _validationName(), "value", "NULL");
				else
					throw new AnnotatedException(errorMessage, e).annotate("validation.name", _validationName(), "value", data.toString());
			}

		}
		return result;		
	}
	
}
