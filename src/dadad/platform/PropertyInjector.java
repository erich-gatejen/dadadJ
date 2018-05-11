package dadad.platform;

/**
 * Property injector.
 */
public class PropertyInjector {

	// ===============================================================================
	// = FIELDS

	
	// ===============================================================================
	// = METHODS
	
	public static void inject(final PropertyView props, final String item) {

		int spot = item.indexOf(PropertyView.PROPERTY_TEXT_EQUALITY);
		if (spot < 1) throw new RuntimeException("Bad property.  No equals (=) found.");
		
		String path = item.substring(0, spot).trim();
		if ((spot + 1) >= item.length()) {
			props.set(path, "");                		        	
		} else {
			props.set(path, PropertyView.decode(item.substring(spot + 1).trim()));	                		        	
		}
		
	}
    
	public static void inject(final PropertyView props, final String[] items) {

		for (String item : items) {
    		inject(props, item);
		}
		
	}

}
