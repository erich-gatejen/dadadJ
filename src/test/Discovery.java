package test;

import dadad.platform.ContextRunnable;
import dadad.platform.test.Group;
import dadad.platform.test.GroupDynamic;
import dadad.platform.test.TestSpecification;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Discover test files.  Currently we only check .class files within the current classpath.
 */
public class Discovery {

	// ===============================================================================
	// = FIELDS

	//final private Configuration config;
	//final private PropertyView properties;
	private final AtomicLong index = new AtomicLong();


	// ===============================================================================
	// = METHODS


    /**
     * Get test specifications under the given root.
     * @param root
     * @return
     */
	public static Group getTestSpecifications(final String root) {

		// Set up root group
        Group rootGroup = new GroupDynamic(root);

		// Discovery process
		String normalRoot = root.trim();
		while ((normalRoot.length() > 0) && (normalRoot.charAt(0) == '/')) normalRoot.substring(1);
        while ((normalRoot.length() > 0) && (normalRoot.charAt(0) == '\\')) normalRoot.substring(1);
        String classRoot = normalRoot.replace('/', '.').replace('\\', '.');
        ArrayList<String> elementsRootList = new ArrayList<String>(Arrays.asList(normalRoot.split("\\.")));
        // If it is an empty element as thelist, empty the list completely so it is ignored below.
        if ((elementsRootList.size() == 1) && (elementsRootList.get(0).length() ==0)) elementsRootList.remove(0);

		try {
            ClassLoader currentClassloader = Thread.currentThread().getContextClassLoader();
            if (currentClassloader == null) throw new RuntimeException("Can't get class loader.");

            // Get classpath and include any elements
            String classpath = System.getProperty("java.class.path");
            String[] classpathEntries = classpath.split(File.pathSeparator);
            for (final String classpathEntry : classpathEntries) {

                // No files, only directories.
                File targetDir = new File(classpathEntry);

                if ( targetDir.isDirectory() ) {

                    while (elementsRootList.size() > 0) {
                        targetDir = new File(targetDir, elementsRootList.remove(0));
                    }

                    if (targetDir.isDirectory()) {
                        // Ok, we have our root.
                        rootGroup.add(spiderDirectory4Classes(targetDir, classRoot, classRoot));

                    }

                }

            }

		} catch (final SecurityException se) {
			throw new RuntimeException (
					"Classloader security problem.", se);
		}

		if (rootGroup.numberOfChildren() == 1) {
            ContextRunnable cr = rootGroup.peekChild(0);
            if (cr instanceof Group) rootGroup = (Group) cr;
        }
		return rootGroup;
	}


    // ===============================================================================
    // = PRIVATE

    private static Group spiderDirectory4Classes(final File targetDir,
                                                 final String classRoot,
                                                 final String name) {

	    Group thisGroup = new GroupDynamic(name);

	    File[] targets = targetDir.listFiles();
	    for (File target : targets) {

	        if (target.isDirectory()) {

                thisGroup.add(spiderDirectory4Classes(target, classRoot + '.' + target.getName(),
                        target.getName()));

            } else if (target.getAbsolutePath().endsWith(".class")) {

	            String cname = target.getName().substring(0, target.getName().length() - ".class".length());
                TestSpecification testSpec = getTestSpec(classRoot + '.' + cname);
                if (testSpec != null) {
                    thisGroup.add(testSpec);
                }

            }

        }

        return thisGroup;
    }

    private static TestSpecification getTestSpec(final String className) {
	    TestSpecification result = null;
	    try {
            Class<?> clazz = Class.forName(className);
            if ((TestSpecification.class.isAssignableFrom(clazz)) &&
                    (! clazz.getName().equals(TestSpecification.class.getName()))){
                result = (TestSpecification) clazz.newInstance();
            }

        } catch (Exception e) {
	        throw new RuntimeException("Class loading problem.  This should not have happened.", e);
        }
	    return result;
    }


}

