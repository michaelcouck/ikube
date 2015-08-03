package ikube.file;

import org.junit.Test;

/**
 * This class corrects Eclipse classpaths, i.e. the '.classpath' files. It looks at the files, tries to
 * find the jars that they refer to, if they don't exist then it looks for the files, and replaces the path
 * with the one that exists in the .classpath file.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 16-07-2015
 */
public class EclipseClasspathCleanerTest {

    @Test
    public void cleanEclipseClasspath() {
        EclipseClasspathCleaner eclipseClasspathCleaner = new EclipseClasspathCleaner();
        eclipseClasspathCleaner.cleanEclipseClasspath("/home/laptop/Workspace/cs");
    }

}
