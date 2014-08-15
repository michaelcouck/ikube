package ikube.toolkit;

import ikube.AbstractTest;
import ikube.IConstants;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-01-2012
 */
public class StringUtilitiesTest extends AbstractTest {

    @Test
    public void isNumeric() {
        assertTrue(StringUtilities.isNumeric("123"));
        assertTrue(StringUtilities.isNumeric("123.456"));
        assertFalse(StringUtilities.isNumeric("123.456.789"));
        assertFalse(StringUtilities.isNumeric("123.456,789"));
        assertFalse(StringUtilities.isNumeric("abc"));
        assertFalse(StringUtilities.isNumeric("123 456"));
        assertFalse(StringUtilities.isNumeric("."));
        assertFalse(StringUtilities.isNumeric("1."));
        assertFalse(StringUtilities.isNumeric(".1"));

        double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
            public void execute() throws Throwable {
                StringUtilities.isNumeric("123.456,789");
            }
        }, "Is numeric", 1000, Boolean.TRUE);
        logger.info("Per second : " + executionsPerSecond);

        executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
            public void execute() throws Throwable {
                StringUtils.isNumeric("123.456,789");
            }
        }, "Is numeric", 1000, Boolean.TRUE);
        logger.info("Per second : " + executionsPerSecond);
    }

    @Test
    public void strip() {
        String stripped = StringUtilities.strip("Michael Couck", IConstants.STRIP_CHARACTERS);
        assertEquals("Michael Couck", stripped);
        stripped = StringUtilities.strip("Michael Couck " + IConstants.STRIP_CHARACTERS, IConstants.STRIP_CHARACTERS);
        assertEquals("Michael Couck ", stripped);
        stripped = StringUtilities.strip("\"Michael \"Couck \"stripped " + IConstants.STRIP_CHARACTERS, "\"");
        assertEquals(" Michael Couck stripped " + IConstants.STRIP_CHARACTERS, stripped);
        stripped = StringUtilities.strip("\" Michael \"Couck \" stripped " + IConstants.STRIP_CHARACTERS, IConstants.STRIP_CHARACTERS);
        assertEquals("\" Michael \"Couck \" stripped ", stripped);

        double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
            public void execute() throws Throwable {
                StringUtilities.strip("Michael ,[]{};:/\\.-_ Couck", IConstants.STRIP_CHARACTERS);
            }
        }, "Strip characters ", 1000, Boolean.TRUE);
        logger.info("Per second : " + executionsPerSecond);
        assertTrue(executionsPerSecond > 100);
    }

    @Test
    public void stripToAlphaNumeric() {
        String string = "Hello � World";
        string = StringUtilities.stripToAlphaNumeric(string);
        assertEquals("Hello World", string);

        string = StringUtilities.stripToAlphaNumeric("hello world.&#");
        assertNotSame("hello world.", string);

        string = StringUtilities.stripToAlphaNumeric("hello world.");
        assertEquals("hello world.", string);

        string = StringUtilities.stripToAlphaNumeric("Hello world.  How are you?  ##@");
        assertEquals("Hello world. How are you?", string);

        string = StringUtilities.stripToAlphaNumeric("123456789-9876543210");
        assertEquals("123456789-9876543210", string);

        string = StringUtilities.stripToAlphaNumeric("&@é123456789-9876543210%%$");
        assertEquals("é123456789-9876543210", string);

        PerformanceTester.execute(new PerformanceTester.APerform() {
            @Override
            public void execute() throws Throwable {
                StringUtilities.stripToAlphaNumeric("&@é123456789-9876543210%%$");
            }
        }, "Strip performance : ", 10000, false);
    }

}
