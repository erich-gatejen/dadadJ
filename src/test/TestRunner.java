package test;

import dadad.data.model.Result;
import dadad.platform.PropertyView;
import dadad.platform.services.PlatformInterfaceRequestor;
import dadad.platform.test.Group;

/**
 * Test runner.  Discover the tests that run them by invoking the root group.
 */
public class TestRunner {

	// ===============================================================================
	// = FIELDS

    private final PropertyView properties;


    // ===============================================================================
	// = METHODS

    /**
     * ctor.
     * @param properties properties
     */
	public TestRunner(final PropertyView properties) {
		this.properties = properties;
    }

    /**
     * ctor.
     * @param runner an existing test runner.
     */
	protected TestRunner(final TestRunner runner) {
		this.properties = runner.properties.shadow();
	}

    /**
     * Run all tests found under the root.
     * @param root the root.  It may be empty to run everything discovered.  See Discovery for informaton on
     *             what is discoverable.
     * @return the results of the test.
     */
	public Result run(final String root) {

        TestContext context = new TestContext(properties);
        PlatformInterfaceRequestor.setPlatformInterface(context);

        Group rootGroup = Discovery.getTestSpecifications(root);
        Result result = rootGroup.run("", context, null);
        return result;

    }

	// ===============================================================================
	// = INTERNAL
	

	
}

