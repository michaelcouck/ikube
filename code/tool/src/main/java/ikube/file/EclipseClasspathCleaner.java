package ikube.file;

import ikube.toolkit.FILE;
import ikube.toolkit.XML;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * This class corrects Eclipse classpaths, i.e. the '.classpath' files. It looks at the files, tries to
 * find the jars that they refer to, if they don't exist then it looks for the files, and replaces the path
 * with the one that exists in the .classpath file.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 16-07-2015
 */
public class EclipseClasspathCleaner {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void cleanEclipseClasspath(final String projectDirectoryString) {
        // Get all the .classpath files
        // From the 'path' entry in the files, can we find the jar that it refers to, relative to the base of the project?
        // If not then from the first directory in the path of the library look for the jar.
        try {
            final Path projectDirectory = Paths.get(projectDirectoryString);
            Files.walkFileTree(projectDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path path, final BasicFileAttributes basicFileAttributes) throws IOException {
                    if (path.getFileName().endsWith(".classpath")) {
                        try {
                            logger.debug("Classpath file : " + path);
                            byte[] bytes = Files.readAllBytes(path);
                            InputStream inputStream = new ByteArrayInputStream(bytes);
                            Document document = XML.getDocument(inputStream, StandardCharsets.UTF_8.name());
                            List<Element> pathElements = XML.getElements(document.getRootElement(), "classpathentry");

                            for (final Element pathElement : pathElements) {
                                for (final Object object : pathElement.attributes()) {
                                    Attribute attribute = (Attribute) object;
                                    String jarPathString = attribute.getValue();
                                    if (!jarPathString.endsWith(".jar") || jarPathString.endsWith(".zip")) {
                                        continue;
                                    }
                                    File jarFile = new File(projectDirectory.getParent().toFile(), jarPathString);
                                    logger.debug("Jar path attribute : " + jarPathString + ", " + jarFile.getAbsolutePath());
                                    if (!jarFile.exists()) {
                                        // Get the first segment of the jar path
                                        Path jarPath = Paths.get(jarPathString);
                                        String lookInThisDirectory = jarPath.iterator().next().toString();
                                        File startDirectory = FILE.findDirectoryRecursively(projectDirectory.toFile(), lookInThisDirectory);
                                        File foundJarFile = FILE.findFileRecursively(startDirectory, jarPath.toFile().getName());
                                        if (foundJarFile == null || !foundJarFile.exists()) {
                                            startDirectory = projectDirectory.toFile();
                                            foundJarFile = FILE.findFileRecursively(startDirectory, jarPath.toFile().getName());
                                        }
                                        if (foundJarFile == null || !foundJarFile.exists()) {
                                            logger.warn("Couldn't find jar file : " + jarPath.toFile().getName());
                                        } else {
                                            String classPathAttributeValue = foundJarFile.getAbsolutePath().replace(projectDirectory.getParent().toFile().getAbsolutePath(), "");
                                            logger.info("Found jar file : " + foundJarFile + ", " + classPathAttributeValue);
                                            attribute.setValue(classPathAttributeValue);
                                            logger.info("Attribute : " + attribute);
                                        }
                                    }
                                }
                            }

                            Files.write(path, document.asXML().getBytes());
                        } catch (final Exception e) {
                            logger.error(null, e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (final IOException e) {
            logger.error(null, e);
        }
    }

}
