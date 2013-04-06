package ikube.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 10.01.2012
 * @version 01.00
 */
public class StringUtilitiesTest extends AbstractTest {

	public StringUtilitiesTest() {
		super(StringUtilitiesTest.class);
	}

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

}
