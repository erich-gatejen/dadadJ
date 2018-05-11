package test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dadad.data.model.Result;
import dadad.platform.AnnotatedException;
import dadad.platform.PropertyInjector;
import dadad.platform.PropertyStore;
import dadad.platform.PropertyView;
import dadad.platform.config.Configuration;
import dadad.platform.config.ContextConfiguration;
import dadad.platform.services.PlatformInterfaceRequestor;
import dadad.platform.test.Group;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test crawler and runner.
 */
public class TestCommander {

	// ===============================================================================
	// = FIELDS

    private final PropertyView properties;
    private Configuration config;


	// ===============================================================================
	// = ENTRY POINT

    public static void main(String[] args){
        PropertyStore testStore = new PropertyStore();
        TestCommander testRunner = new TestCommander(testStore);
        try {
            testRunner.run(args);
        } catch (Exception e) {
            System.out.println(AnnotatedException.render(e, true));
        }
    }


    // ===============================================================================
	// = METHODS

	public TestCommander(final PropertyView properties) {
		this.properties = properties;
		config = new Configuration(properties, false, false);
    }


	public void run(final String[] args) throws Exception {

        // Gather properties
        if (args.length > 0) {
            PropertyInjector.inject(properties, Arrays.copyOfRange(args, 0, args.length));
        }

        String propFilePath = config.get(ContextConfiguration.CONTEXT_PROP_FILE, false);
        if (propFilePath != null) {
            File propFile = new File(propFilePath);
            if (propFile.canRead()) {
                properties.load(propFile);
            } else {
                System.out.println("Properties file does not exist so ignoring it.  file=" + propFile.getAbsolutePath());
            }
        }

        // Configuration points
        String rootDir = config.get(TestConfiguration.TEST_ROOT);

        TestOutputType outputType = (TestOutputType) config.getEnum(TestConfiguration.TEST_RESULT_OUTPUT_TYPE);
        OutputStream outputStream = System.out;
        String outputFile = config.get(TestConfiguration.TEST_RESULT_FILE);
        if (outputFile != null) outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        TestOutputType errorOutputType = (TestOutputType) config.getEnum(TestConfiguration.TEST_RESULT_ERROR_TYPE);
        OutputStream errorOutputStream = System.out;
        String errorOutputFile = config.get(TestConfiguration.TEST_ERROR_FILE);
        if (errorOutputFile != null) errorOutputStream = new BufferedOutputStream(new FileOutputStream(errorOutputFile));

        // Configure and run tests
        TestContext context = new TestContext(properties);
        PlatformInterfaceRequestor.setPlatformInterface(context);

        Group rootGroup = Discovery.getTestSpecifications("test.dadad");
        Result result = rootGroup.run("", context, null);

        // Dump full results
        dump(outputType, result, outputStream, "full");

        // Dump error results
        Result errorResult = result.pruneNotFailed();
        dump(errorOutputType, errorResult, errorOutputStream, "error");

    }


	// ===============================================================================
	// = INTERNAL
	
    private void dump(final TestOutputType outputType, final Result result, final OutputStream os,
                      final String whichResultName) throws Exception {
        try {


            switch(outputType) {
                case JSON:
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    getPrintStream(os).println(gson.toJson(result));
                    break;

                case YAML:
                    Yaml yaml = new Yaml();
                    OutputStreamWriter writer = new OutputStreamWriter(os);
                    yaml.dump(result, writer);
                    writer.flush();
                    break;

                case TEXT:
                    getPrintStream(os).println(result.renderLongForm());
                    break;

                case LINE:
                    getPrintStream(os).println(result.renderLineForm());
                    break;

                case NONE:
                    break;
            }


        } catch (Exception e) {
            throw new RuntimeException("Could not format " + whichResultName + " result output.", e);
        }

        os.flush();
    }

    private PrintStream getPrintStream(final OutputStream os) {
        if (os instanceof PrintStream) return (PrintStream) os;
        return new PrintStream(os);
    }
	
}

