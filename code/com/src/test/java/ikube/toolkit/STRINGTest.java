package ikube.toolkit;

import ikube.AbstractTest;
import ikube.Constants;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-01-2012
 */
public class STRINGTest extends AbstractTest {

    @Test
    public void isNumeric() {
        assertTrue(STRING.isNumeric("123"));
        assertTrue(STRING.isNumeric("123.456"));
        assertFalse(STRING.isNumeric("123.456.789"));
        assertFalse(STRING.isNumeric("123.456,789"));
        assertFalse(STRING.isNumeric("abc"));
        assertFalse(STRING.isNumeric("123 456"));
        assertFalse(STRING.isNumeric("."));
        assertFalse(STRING.isNumeric("1."));
        assertFalse(STRING.isNumeric(".1"));

        double executionsPerSecond = PERFORMANCE.execute(new PERFORMANCE.APerform() {
            public void execute() throws Throwable {
                STRING.isNumeric("123.456,789");
            }
        }, "Is numeric", 1000, Boolean.TRUE);
        logger.info("Per second : " + executionsPerSecond);

        executionsPerSecond = PERFORMANCE.execute(new PERFORMANCE.APerform() {
            public void execute() throws Throwable {
                StringUtils.isNumeric("123.456,789");
            }
        }, "Is numeric", 1000, Boolean.TRUE);
        logger.info("Per second : " + executionsPerSecond);
    }

    @Test
    public void strip() {
        String stripped = STRING.strip("Michael Couck", Constants.STRIP_CHARACTERS);
        assertEquals("Michael Couck", stripped);
        stripped = STRING.strip("Michael Couck " + Constants.STRIP_CHARACTERS, Constants.STRIP_CHARACTERS);
        assertEquals("Michael Couck ", stripped);
        stripped = STRING.strip("\"Michael \"Couck \"stripped " + Constants.STRIP_CHARACTERS, "\"");
        assertEquals(" Michael Couck stripped " + Constants.STRIP_CHARACTERS, stripped);
        stripped = STRING.strip("\" Michael \"Couck \" stripped " + Constants.STRIP_CHARACTERS, Constants.STRIP_CHARACTERS);
        assertEquals("\" Michael \"Couck \" stripped ", stripped);

        double executionsPerSecond = PERFORMANCE.execute(new PERFORMANCE.APerform() {
            public void execute() throws Throwable {
                STRING.strip("Michael ,[]{};:/\\.-_ Couck", Constants.STRIP_CHARACTERS);
            }
        }, "Strip characters ", 1000, Boolean.TRUE);
        logger.info("Per second : " + executionsPerSecond);
        assertTrue(executionsPerSecond > 100);
    }

    @Test
    public void stripToAlphaNumeric() {
        String string = "Hello � World";
        string = STRING.stripToAlphaNumeric(string);
        assertEquals("Hello World", string);

        string = STRING.stripToAlphaNumeric("hello world.&#");
        assertNotSame("hello world.", string);

        string = STRING.stripToAlphaNumeric("hello world.");
        assertEquals("hello world.", string);

        string = STRING.stripToAlphaNumeric("Hello world.  How are you?  ##@");
        assertEquals("Hello world. How are you?", string);

        string = STRING.stripToAlphaNumeric("123456789-9876543210");
        assertEquals("123456789-9876543210", string);

        string = STRING.stripToAlphaNumeric("&@é123456789-9876543210%%$");
        assertEquals("é123456789-9876543210", string);

        PERFORMANCE.execute(new PERFORMANCE.APerform() {
            @Override
            public void execute() throws Throwable {
                STRING.stripToAlphaNumeric("&@é123456789-9876543210%%$");
            }
        }, "Strip performance : ", 10000, false);
    }

    @Test
    public void isDate() {
        boolean isDate = STRING.isDate("");
        assertFalse(isDate);
        isDate = STRING.isDate("1971-05-27-12-12-12");
        assertTrue(isDate);
        isDate = STRING.isDate("71-5-7-12-12-12");
        assertTrue(isDate);
        isDate = STRING.isDate("71-5-7");
        assertTrue(isDate);
    }

}