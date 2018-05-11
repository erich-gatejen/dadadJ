package dadad.platform.test;

/**
 * A dynamic test that can be build and mutated at runtime.
 * 
 */
public class GroupDynamic extends Group {

	// ===============================================================================
	// = ABSTRACT

	/**
	 * Define the group by making calls to the DEFINITION METHODS (or not).
	 */
	public void _define() {
		// NOP
	}

	// ===============================================================================
	// = INTERFACE

    public String name() {
	    return name;
    }


	// ===============================================================================
	// = FIELDS

    private final String name;


	// ===============================================================================
	// = METHODS

	/**
	 * Construct the test.
	 */
	public GroupDynamic(final String name) {
		super(name);
		this.name = name;
	}	


	// ===============================================================================
	// = INTERNAL
		

	
}
