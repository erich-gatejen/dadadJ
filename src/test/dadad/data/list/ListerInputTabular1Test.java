package test.dadad.data.list;

import dadad.data.io.CSVReader;
import dadad.data.list.ListerCatalog;
import dadad.data.list.ListerInput;
import dadad.data.model.Block;
import dadad.data.model.BlockType;
import dadad.data.model.Term;
import dadad.platform.test.TestCase;
import dadad.platform.test.TestSpecification;

import java.io.StringReader;

import static dadad.platform.test.TestCase.Type.UNIT;

public class ListerInputTabular1Test extends TestSpecification {

	// ===============================================================================
	// = FIELDS




	// ===============================================================================
	// = ABSTRACT

	public String name() {
		return ListerInputTabular1Test.class.getSimpleName();
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

    public final static String TESTDATA_GOOD1 =
            "BLOCK\t1\tLINE\n" +
                    "\tE\tSTRING\tTAG1\tString 1\n" +
                    "\tE\tTEXT\tTAG2\tText 2\n" +
                    "\tE\tLONG\tTAG3\t716373910\n" +
                    "\tE\tDOUBLE\tTAG4\t12312312.12312312\n" +
                    "\tE\tSTRING\tTAG1\tString\\ttab2\n";

    @TestCase(type = UNIT, priority = 100)
    public void good1() {

        ListerInput lister = ListerCatalog.VERSION__TABULAR1.getInputLister(new StringReader(TESTDATA_GOOD1));

        Block block = lister.getBlock(1);
        verify(block, 1, 1, BlockType.LINE,  new Term("String 1"), new Term("Text 2"));
    }


    // ===============================================================================
    // = VALIDATORS

    private void verify(final Block block, final int blockId,  final int ownerId, final BlockType blockType,
                               final Term... terms) {
        if (block.info.blockId() != blockId) FAIL("Bad block Id.  expected=" + blockId + "  actual=" + block.info.blockId());
        if (block.info.ownerId() != ownerId) FAIL("Bad owner Id.  expected=" + ownerId + "  actual=" + block.info
                .ownerId());
        if (block.info.type() != blockType) FAIL("Bad block type.  expected=" + blockType.name() + "  actual=" + block
                .info.type());

    }

}

