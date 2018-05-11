package dadad.platform.config;

import static dadad.platform.AnnotatedException.Catagory.ERROR;

import java.lang.reflect.Field;
import java.util.Arrays;

import dadad.platform.AnnotatedException;
import dadad.platform.config.validation.Validation;

public interface ConfigurationType {

	public final static String FIELD__DEFAULT_VALUE = "defaultValue";
	public final static String FIELD__PROPERTY = "property";
	public final static String FIELD__HELP = "help";
	public final static String FIELD__VALIDATIONS = "validations";

	default public String defaultValue() {
		return getField(FIELD__DEFAULT_VALUE);	}
	
	default public String property() {
		return getField(FIELD__PROPERTY);
	}
	
	default public String help() {
		return getField(FIELD__HELP);
	}
	
	default public String getField(final String name) {
		try {
			Field field = this.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return (String) field.get(this);
		} catch (Exception e) {
			// There is no help
			return null;
		}
	}
	
	default public Object[] getValidations() {
		try {
			Field field = this.getClass().getDeclaredField(FIELD__VALIDATIONS);
			field.setAccessible(true);
			return (Object[]) field.get(this);
		} catch (Exception e) {
			// There are no validations
			return null;
		}
	}
	
	default public void throwConfigException(final String message) {
		throw new AnnotatedException("Configuration action failed.", ERROR)
			.annotate("property.name", property(), "property.help", help());	
	}
	
	default public void throwConfigException(final String message, Throwable cause) {
		throw new AnnotatedException("Configuration action failed.", ERROR, cause)
			.annotate("property.name", property(), "property.help", help());	
	}
	
	@SuppressWarnings("unchecked")
	default public void validate(final Object data) {
		try {
			Object[] validations = getValidations();
			if (validations == null) return;
			
			for (Object validation : validations) {
				
				if (validation instanceof Validation) {
				
					((Validation) validation).validate(data, "Validating property failed.", true);
					
				} else if ((validation instanceof Class<?>) && (((Class<?>) validation).isEnum())) {
				    if (validations.length > 1)
				        throw new Error("BUG!!!  You can only have a single validation if any validation is an Enum class.");
					
					Class<?> validationClass = (Class<?>) validation;										
					Enum<?> value = findEnumValue((Class<? extends Enum<?>>) validationClass, data.toString());
					if (value == null) {
						throw new AnnotatedException("Not a valid enum value for enum " + validationClass.getName());
					}
					
				}
			}
			
		} catch (AnnotatedException ae) {
			if (data == null) throw ae.annotate("property.name", property(), "property.help", help(), "value", "NULL");
			throw ae.annotate("property.name", property(), "property.help", help(), "value", data.toString());
			
		} catch (Exception e) {
			throwConfigException(e.getMessage(), e);
		}
	}
	
	public static Enum<?> findEnumValue(Class<? extends Enum<?>> enumType, String value) {
	    return Arrays.stream(enumType.getEnumConstants())
	                 .filter(e -> e.name().equals(value))
	                 .findFirst()
	                 .orElse(null);
	}
	
	default public void validate(final String... data) {
		validate((Object[]) data);	// This is a hack to suppress unnecessary warnings in eclipse.
	}
	
	default public void validate(final Object... data) {
		int fieldNum = 0;
		try {
			
			if (data == null) {
				validate((Object) null);

			} else {
				for (Object item : data) {
					validate(item);
					fieldNum++;
				}
			}
			
		} catch (AnnotatedException ae) {
			throw ae.annotate("multivalue.field.number", fieldNum);
		}
		
	}
	
}
