package dadad.platform.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import dadad.platform.AnnotatedException;
import dadad.platform.PropertyView;

/**
 * Configuration class.  It helps manage properties configurations.
 */
public class Configuration {

	// ===============================================================================
	// = FIELDS
	
	private PropertyView properties;
	private boolean readOnly;
	private boolean shadowWrites;
	
	private HashMap<String, String[]> shadows;
	
	// ===============================================================================
	// = METHODS
	
	public Configuration(final PropertyView properties, final boolean readOnly, final boolean shadowWrites) {
		this.properties = properties;
		this.readOnly = readOnly;
		this.shadowWrites = shadowWrites;
		
		if (shadowWrites) shadows = new  HashMap<String, String[]>();
	}
	
	public void cleanShadows() {
		for (String name : shadows.keySet()) {
			properties.set(name, shadows.get(name));
		}
	}

	// -- READS ------------------------------------------------------------
	
	public boolean exists(final ConfigurationType type) {
		return properties.exists(type.property());
	}

	/**
	 * Get a configuration.  If present, it will be validated.
	 * @param type configuration type.
	 * @return the value or null if it doesn't exist.
	 */
	public String get(final ConfigurationType type) {
		return get(type, true);
	}

	/**
	 * Get a configuration by name.  It will not be validated.
	 * @param name configuration name.
	 * @return the value or null if it doesn't exist.
	 */
	public String get(final String name) {
		return properties.get(name);
	}

	/**
	 * Get a configuration by name.  If present, it will be validated.
	 * @param type configuration type.
	 * @param validate if true (default behavior), it will validate the value, if present.  If false it will not validate it.
	 * @return the value or null if it doesn't exist.
	 */
	public String get(final ConfigurationType type, final boolean validate) {
		String value = properties.get(type.property());
		if (value == null) value = type.defaultValue();
		if (value != null) type.validate(value);
		return value;
	}
	
	public String[] getMultivalue(final ConfigurationType type) {
		return getMultivalue(type, true);
	}
	
	public String[] getMultivalue(final ConfigurationType type, final boolean validate) {
		String[] value = properties.getMultivalue(type.property());
		if ((value == null) && (type.defaultValue() != null)) {
			value = new String[1];
			value[0] = type.defaultValue();
		}
		if ((value != null) && (validate)) type.validate(value);
		return value;
	}
	
	public String[] getMultivalue(final String name) {
		return properties.getMultivalue(name);
	}
	
	public String encode(final String[] values) {
		return PropertyView.encode(values);
	}
	
	public String getRequired(final ConfigurationType type) {
		String value = get(type, true);
		if (value == null) throw new AnnotatedException("Required property is not set.", AnnotatedException.Catagory.ERROR)
				.annotate("property", type.property());
		return value;
	}
	
	public String getRequired(final String name) {
		String value = get(name);
		if (value == null) throw new AnnotatedException("Required property is not set.", AnnotatedException.Catagory.ERROR)
				.annotate("property", name);
		return value;
	}
	
	public int getInt(final ConfigurationType type) {
		return getInt(type, get(type));
	}
	
	public int getInt(final ConfigurationType type, final String value) {
		int result;
		try {
			result = Integer.parseInt(value);
		} catch (Exception e) {
			throw new AnnotatedException("Property is not a value int.", AnnotatedException.Catagory.ERROR, e)
					.annotate("property", type.property(), "value", get(type));
		}
		return result;
	}
	
	public int getRequiredInt(final ConfigurationType type) {
		return getInt(type, getRequired(type)); 
	}
	
	public int getBoundedInt(final ConfigurationType type, final int lower, final int upper) {
		int value = getRequiredInt(type);
		if ((value < lower) || (value > upper)) {
			throw new AnnotatedException("Property is value as an int is out of bounds.", AnnotatedException.Catagory.ERROR)
				.annotate("property", type.property(), "value", get(type), "lower.bound", lower, "upper.bound", upper);
			
		}
		return value; 
	}
	
	public int[] getIntMultivalue(final ConfigurationType configType) {
		return getIntMultivalue(configType, false);
	}
	
	public int[] getIntMultivalue(final ConfigurationType configType, final boolean required) {
		String[] values = getMultivalue(configType);
		if ((values == null) || (values.length == 0)) {
			if (required) throw new AnnotatedException("Has no values but it is required.")
					.annotate("property", configType.property());			
			return new int[0];
		}
			
		int[] result = new int[values.length];
		for (int index = 0; index < values.length; index++) {
			try {
				result[index] = Integer.parseInt(values[index]);				
			} catch (NumberFormatException e) {
				throw new AnnotatedException("Element to alter position not a valid number.", e)
					.annotate("property", configType.property(), "value.position", index, "text", values[index]);
			}
		}
		return result;
	}
	
	public long getLong(final ConfigurationType type) {
		return getLong(type, get(type));
	}
	
	public long getLong(final ConfigurationType type, final String value) {
		long result;
		try {
			result = Long.parseLong(value);
		} catch (Exception e) {
			throw new AnnotatedException("Property is not a value Long.", AnnotatedException.Catagory.ERROR, e)
					.annotate("property", type.property(), "value", get(type));
		}
		return result;
	}
	
	public long getRequiredLong(final ConfigurationType type) {
		return getLong(type, getRequired(type)); 
	}
	
	public boolean getBoolean(final ConfigurationType type) {
		boolean result;
		try {
			result = Boolean.parseBoolean(get(type));
		} catch (Exception e) {
			throw new AnnotatedException("Property is not a value boolean.", AnnotatedException.Catagory.ERROR, e)
					.annotate("property", type.property(), "value", get(type));
		}
		return result;
	}
	
	/**
	 * Get a ply.  It will clip the properties name up to the ply.
	 * @param type
	 * @return
	 */
	public Map<String, String[]> getPly(final ConfigurationType type) {
		HashMap<String, String[]> result = new HashMap<String, String[]>();
		for (String item : properties.ply(type.property())) {
			result.put(item, properties.getMultivalue(item));
		}
		return result;
	}
	
	public boolean getRequiredBoolean(final ConfigurationType type) {
		getRequired(type);
		return getBoolean(type); 
	}

	/**
	 * Get an enum object for the given configuration.
	 * @param type configuration type.  It must have a single validation that is the enum's class.
	 * @return the Enum value or null if not set.
	 */
	public Enum<?> getEnum(final ConfigurationType type) {
		String value = get(type);

		// This should work since the value was already validated and there is only one of them.
		Class<?> validationClass = (Class<?>) (type.getValidations()[0]);
		Enum<?> result = ConfigurationType.findEnumValue((Class<? extends Enum<?>>) validationClass, value);
		return result;
	}

	/**
	 * Get an enum object for the given configuration.
	 * @param type configuration type.  It must have a single validation that is the enum's class.
	 * @return the Enum value or null if not set.
	 */
	public Enum<?> getRequiredEnum(final ConfigurationType type) {
		Enum<?> result = getEnum(type);
		if (result == null) throw new AnnotatedException("Required property is not set.  Expecting an enum value.",
				AnnotatedException.Catagory.ERROR).annotate("property", type.property());
		return result;
	}

	// -- WRITES ------------------------------------------------------------
	
	public void set(final ConfigurationType type, final String value) {
		set(type, value, false);
	}
	
	public void set(final ConfigurationType type, final String value, final boolean decode) {
		checkReadOnly();
		type.validate(value);
		if (shadowWrites) shadow(type);
		properties.set(type.property(), value, decode);
	}	
	
	public void set(final String name, final String value) {
		set(name, value, false);
	}
	
	public void set(final String name, final String value, final boolean decode) {
		checkReadOnly();
		if (shadowWrites) shadow(name);
		properties.set(name, value, decode);
	}	
	
	public void set(final ConfigurationType type, final String... value) {
		checkReadOnly();
		type.validate(value);
		if (shadowWrites) shadow(type);
		properties.set(type.property(), value);
	}	
	
	public void set(final String name, final String... value) {
		checkReadOnly();
		if (shadowWrites) shadow(name);
		properties.set(name, value);
	}	
	
	public void load(final File file) {
		properties.load(file);
	}
	
	public void load(final String filePath) {
		try {
			properties.load(new BufferedReader(new FileReader(filePath)), filePath);
		} catch (FileNotFoundException e) {
			throw new AnnotatedException("File does not exist").annotate("file.path", filePath);
		}
	}
	
	// ===============================================================================
	// = INTERNAL
	
	private void checkReadOnly() {
		if (readOnly) throw new AnnotatedException("This configuration is read only.", AnnotatedException.Catagory.FAULT);		
	}
	
	private void shadow(final ConfigurationType type) {
		String[] value = getMultivalue(type);
		if (value != null) {
			shadows.put(type.property(), value);
		}
	}
	
	private void shadow(final String name) {
		String[] value = getMultivalue(name);
		if (value != null) {
			shadows.put(name, value);
		}
	}
	
}
