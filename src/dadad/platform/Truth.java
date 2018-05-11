package dadad.platform;

/**
 * Decides the truth of an object.  Why an object?  The original version did a lot more.  I might get around to
 * porting it eventually.
 * 
 * For Strings, 'true' (of any case) and 'yes' (of any case) are true, whereas everything else is false.
 * For Boolean, it will be the value of the Boolean.
 * For Numerics, 0 is false and anything else is true.
 * A null reference is always false.
 * Any other object passed will cause an exception.
 */
public class Truth {

	// ===============================================================================
	// = FIELDS
	
	public final boolean truth;

	// ===============================================================================
	// = METHOD
	
	public Truth(final Object truthObject) {
		truth = truth(truthObject);
	} 
	
    public static boolean truth(final Object truthObject) {
        boolean truth;
        if (truthObject == null) {
            truth = false;

        } else if (truthObject instanceof String) {
            String value = ((String) truthObject).trim();
            if ((value.equalsIgnoreCase("true")) || (value.equalsIgnoreCase("yes"))) {
                truth = true;

            } else {
                truth = false;
            }

        } else if (truthObject instanceof Integer) {
            if ( ((Integer) truthObject).intValue() == 0) {
                truth = false;
            } else {
                truth = true;
            }

        } else if (truthObject instanceof Long) {
            if ( ((Long) truthObject).longValue() == 0) {
                truth = false;
            } else {
                truth = true;
            }

        } else if (truthObject instanceof Float) {
            if ( ((Float) truthObject).floatValue() == 0) {
                truth = false;
            } else {
                truth = true;
            }

        } else if (truthObject instanceof Double) {
            if ( ((Double) truthObject).doubleValue() == 0) {
                truth = false;
            } else {
                truth = true;
            }

        } else if (truthObject instanceof Boolean) {
            truth = ((Boolean) truthObject).booleanValue();

        } else {
            throw new RuntimeException("Cannot get truth from a " + truthObject.getClass().getName() + " object.");
        }

        return truth;
    }
}

