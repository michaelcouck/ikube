package ikube.toolkit;

import ikube.AbstractTest;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PatternTest extends AbstractTest {

    @Test
    public void match() {
        String regex = "(http://|https://).*";

        String input = "http://www.oki.com";
        boolean matches = Pattern.matches(regex, input);
        logger.info("Matches : " + matches);
        assertTrue(matches);

        input = "https://www.oki.com";
        matches = Pattern.matches(regex, input);
        logger.info("Matches : " + matches);
        assertTrue(matches);

        input = "svn://www.oki.com";
        matches = Pattern.matches(regex, input);
        logger.info("Matches : " + matches);
        assertFalse(matches);

        input = "svn://https://www.oki.com";
        matches = Pattern.matches(regex, input);
        logger.info("Matches : " + matches);
        assertFalse(matches);

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String link = matcher.group();
            logger.info("Link : " + link);
        }

        pattern = Pattern.compile(".*support.*|.*\\.pdf.*");
        input = "http://www.ibm.com/support/us/search/index.html";
        boolean isExcluded = pattern.matcher(input).matches();
        assertTrue(isExcluded);

        input = "http://www.ibm.com/us/search/index.html";
        isExcluded = pattern.matcher(input).matches();
        assertFalse(isExcluded);

        input = "http://www.ibm.com/us/search/pdf.pdf";
        isExcluded = pattern.matcher(input).matches();
        assertTrue(isExcluded);

        pattern = Pattern.compile(".*serenity.odb");
        input = "file:/tmp/modules/Jar/serenity/serenity.odb";
        boolean included = pattern.matcher(input).matches();
        assertTrue(included);

        pattern = Pattern.compile(".*serenity.*.source.*");
        input = "file:/tmp/serenity/source/";
        included = pattern.matcher(input).matches();
        assertTrue(included);
        input = "file:\\tmp\\workspace\\serenity\\source\\";
        included = pattern.matcher(input).matches();
        assertTrue(included);

        pattern = Pattern.compile("and|or|not");
        input = "world";
        included = pattern.matcher(input).matches();
        assertFalse(included);
    }

}
