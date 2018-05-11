package test.dadad.data.io.test.dadad.data.io.gobbler;

import dadad.data.io.gobbler.GobblerProgram;
import dadad.data.model.Term;
import dadad.platform.test.TestCase;
import dadad.platform.test.TestSpecification;

import java.io.StringReader;
import java.util.List;

import static dadad.platform.test.TestCase.Type.COMPONENT;
import static dadad.platform.test.TestCase.Type.UNIT;

public class GobblerTest extends TestSpecification {

	// ===============================================================================
	// = FIELDS




	// ===============================================================================
	// = ABSTRACT

	public String name() {
		return GobblerTest.class.getSimpleName();
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

    public static final String TEST_DATA1 = "123456789";
    public static final String TEST_CHOP_1 = "CHOP,5\nCHOP,4\n";
    public static final String TEST_CHOP_2 = "CHOP,4\nREST\n";
    public static final String TEST_CHOP_3 = "CHOP,9\n";
    public static final String TEST_CHOP_4 = "CHOP,10\n";
    public static final String TEST_CHOP_5 = "CHOP,10\nCHOP5\n";

    @TestCase(type = UNIT, priority = 100)
    public void chop() {
        expectPass("chop1", TEST_CHOP_1, TEST_DATA1, "12345", "6789");
        expectPass("chop2", TEST_CHOP_2, TEST_DATA1, "1234", "56789");
        expectPass("chop3", TEST_CHOP_3, TEST_DATA1, "123456789");
        expectPass("chop4", TEST_CHOP_4, TEST_DATA1, "123456789");
        expectException("chop5", TEST_CHOP_5, TEST_DATA1, "x");
    }

    public static final String TEST_DATA2 = "123456789xxx,BORK,asdfasdf$   adsfasdf  !!asdf$xdfasdf";
    public static final String TEST_BLOCK_1 = "BLOCK,\",\",\",\"";
    public static final String TEST_BLOCK_2 = "BLOCK,$,!\nBLOCK,!,$";
    public static final String TEST_BLOCK_3 = "BLOCK,[,],error";
    public static final String TEST_BLOCK_4 = "BLOCK,[,],empty";
    public static final String TEST_BLOCK_5 = "BLOCK,[,],ignore";
    public static final String TEST_BLOCK_6 = "BLOCK,$,$,rest\nREST";

    @TestCase(type = UNIT, priority = 100)
    public void block() {
        expectPass("block1", TEST_BLOCK_1, TEST_DATA2, "BORK");
        expectPass("block2", TEST_BLOCK_2, TEST_DATA2, "   adsfasdf  ", "asdf");
        expectException("block3", TEST_BLOCK_3, TEST_DATA2, "   adsfasdf  ", "asdf");
        expectPass("block4", TEST_BLOCK_4, TEST_DATA2, "");
        expectNone("block5", TEST_BLOCK_5, TEST_DATA2, "");
        expectPass("block6", TEST_BLOCK_6, TEST_DATA2, "   adsfasdf  !!asdf", "xdfasdf");
    }

    public static final String TEST_DATA3 = "12345[67890]  aa [377192 273.292]asdfasdf[qqqqqqqqqqq] sadvasdf";
    public static final String TEST_SPLIT_1 = "SPLIT,\\[(.*?)\\]";
    public static final String TEST_SPLIT_2 = "SPLIT,\\[(.*?)\\]\nREST";

    @TestCase(type = UNIT, priority = 100)
    public void split() {
        expectPass("split1", TEST_SPLIT_1, TEST_DATA3, "67890", "377192 273.292", "qqqqqqqqqqq");
        expectPass("split1", TEST_SPLIT_2, TEST_DATA3, "67890", "377192 273.292", "qqqqqqqqqqq");
    }

    public static final String TEST_DATA4 = "asdf  asdfasdf   fdsa  \n  qqqqqqq www  ";
    public static final String TEST_TERM_1 = "TERM\nTERM\nTERM\nTERM\nTERM\nTERM";

    @TestCase(type = UNIT, priority = 100)
    public void term() {
        expectPass("term1", TEST_TERM_1, TEST_DATA4, "asdf", "asdfasdf", "fdsa", "qqqqqqq", "www");
    }

    public static final String TEST_DATA5 = "12345  TERM  asdfasdf[BLOCK]REST";
    public static final String TEST_COMBO_1 = "CHOP,5\nTERM\nBLOCK,[,]\nREST";

    @TestCase(type = COMPONENT, priority = 100)
    public void combo() {
        expectPass("term1", TEST_COMBO_1, TEST_DATA5, "12345", "TERM", "BLOCK", "REST");
    }

    // ===============================================================================
    // = VALIDATORS

    private void verify(final List<Term> terms, final String data, final String... fields ) {
        if (fields.length != terms.size()) FAIL("Not the correct number of expected values.  Expected="
                + fields.length + "  Actual=" + terms.size() + "  for Data=[" + data + "]");
        int index = 0;
        for (Term term : terms) {
            if (! fields[index].equals(term.text)) FAIL("Terms did not match.  Expected=["
                    + fields[index] + "]  Actual=[" + term.text + "]  Term number=" + index + "");
            index++;
        }
    }

    // ===============================================================================
    // = INTERNAL

    private GobblerProgram prog(final String name, final String program) {
	    return new GobblerProgram(name, new StringReader(program));
    }

    private void expectPass(final String name, final String prog, final String data, final String... resultTerms) {
        List<Term> terms = prog(name, prog).execute(data);
        verify(terms, data, resultTerms);
    }

    private void expectNone(final String name, final String prog, final String data, final String... resultTerms) {
        List<Term> terms = prog(name, prog).execute(data);
        if (terms.size() > 0) FAIL("Expecting get no terms but got one.  value=[" + terms.get(0).text + "]");
    }

    private void expectException(final String name, final String prog, final String data, final String... resultTerms) {
        try {
            expectPass(name, prog, data, resultTerms);
            FAIL("Expecting exception but did not get one.  name=" + name);

        } catch (Exception e) {
            // NOP
        }
    }
}


