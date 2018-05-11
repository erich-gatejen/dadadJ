package test.dadad.data.io;

import dadad.data.io.CSVReader;
import dadad.data.model.ElementType;
import dadad.platform.test.TestCase;
import dadad.platform.test.TestSpecification;

import static dadad.platform.test.TestCase.Type.UNIT;

public class CSVReaderTest extends TestSpecification {

	// ===============================================================================
	// = FIELDS


	// ===============================================================================
	// = ABSTRACT

	public String name() {
		return CSVReaderTest.class.getSimpleName();
	}

    public void _setup() {
        // NOP
    }

	public void _teardown() {

    }

	public boolean _isTerminal() {
        return true;
    }

	// ===============================================================================
	// = TESTS

    @TestCase(type = UNIT, priority = 100)
    public void wikiExamples() {
        verify("1997,Ford,E350", "1997", "Ford", "E350");
        verify("\"1997\",\"Ford\",\"E350\"", "1997", "Ford", "E350");
        verify("1997,Ford,E350,\"Super, \"\"luxurious\"\" truck\"", "1997", "Ford", "E350", "Super, \"luxurious\" truck");
        verify("1997,Ford,E350,\"Go get one now\nthey are going fast\"", "1997","Ford","E350","Go get one now\nthey are going fast");
        verify("1997,Ford,E350,\" Super luxurious truck \"", "1997", "Ford", "E350", " Super luxurious truck ");
    }


    // ===============================================================================
    // = VALIDATORS

    private void verify(final String data, final String... fields) {
        String[] values = CSVReader.split(data);
        if (fields.length != values.length) FAIL("Not the correct number of expected values.  Expected="
                + fields.length + "  Actual=" + values.length + "  for Line=[" + data + "]");
        for (int index = 0; index < values.length; index++) {
            if (! fields[index].equals(values[index])) FAIL("Terms did not match.  Expected=["
                    + fields[index] + "]  Actual=[" + values[index] + "]  Term number=" + index + "");
        }
    }
}

