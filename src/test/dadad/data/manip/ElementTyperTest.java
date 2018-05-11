package test.dadad.data.manip;

import dadad.data.DataContext;
import dadad.data.manip.ElementTyper;
import dadad.data.model.ElementType;
import dadad.platform.test.TestCase;
import dadad.platform.test.TestSpecification;

import static dadad.platform.test.TestCase.Type.UNIT;

public class ElementTyperTest extends TestSpecification {

	// ===============================================================================
	// = FIELDS

	private ElementTyper typer;

	// ===============================================================================
	// = ABSTRACT

	public String name() {
		return ElementTyperTest.class.getSimpleName();
	}

    public void _setup() {
        typer = new ElementTyper(new DataContext(CONTEXT()));
    }

	public void _teardown() {

    }

	public boolean _isTerminal() {
        return true;
    }

	// ===============================================================================
	// = TESTS

    @TestCase(type = UNIT, priority = 100)
    public void checkText() {
        isText("asdfasdf adfasdf");
        isText(" 123456 ");
        isText("121212.1212 ");
        isText("+ 1212121");
        isText(".");
        isText("@#$(R#$RUVJ)R!@($F*GQUREHF");
        isText("A");
        isText("\r");
        isText("2222 2222");

    }

    @TestCase(type = UNIT, priority = 100)
    public void checkLong() {
        isLong("121201230121203");
        isLong("+121201230121203");
        isLong("-121201230121203");
        isLong("1");
    }

    @TestCase(type = UNIT, priority = 100)
    public void checkDouble() {
	    isDouble(".0001");
        isDouble("124234.2424");
        isDouble("+.292929");
        isDouble("-123234.234324");
    }

    // ===============================================================================
    // = VALIDATORS

    private void isText(final String data) {
        ElementType type = typer.type(data);
        if (type != ElementType.TEXT) fail(ElementType.TEXT, data);
    }

    private void isLong(final String data) {
        ElementType type = typer.type(data);
        if (type != ElementType.LONG) fail(ElementType.LONG, data);
    }

    private void isDouble(final String data) {
        ElementType type = typer.type(data);
        if (type != ElementType.DOUBLE) fail(ElementType.DOUBLE, data);
    }

    private void fail(final ElementType type, final String data) {
        FAIL("Expecting " + type.name() + " for '" + data + "'");
    }

}
