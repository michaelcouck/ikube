package ikube.zip;

import ikube.toolkit.FILE;
import ikube.toolkit.STRING;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class will scan zip files and look for entries that are specified by name.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 08-02-2011
 */
public class ZipLocator {

    static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipLocator.class);

    public static void main(final String[] args) {
        if (args == null || args.length < 3) {
            System.out.println("Usage : java -jar ikube-xxx....jar [class-to-execute] [path/to/folder] [zip-patterns] [file pattern] [contents pattern]");
            System.out.println("example : java -jar ikube-tool-4.4.0.jar ikube.zip.ZipLocator /tmp .*(\\.zip\\Z).*|.*(\\.jar\\Z).*|.*(\\.war\\Z).*|.*(\\.ear\\Z).* SerenityPublisher");
            return;
        }
        final String path = args[0];
        final Pattern fileType = Pattern.compile(args[1]);
        final Pattern fileEntry = Pattern.compile(".*(" + args[2] + ").*");
        final Pattern fileContent = args.length > 3 ? Pattern.compile(".*(" + args[3] + ").*") : null;
        try {
            Files.walkFileTree(new File(path).toPath(), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
                    File file = path.toFile();
                    if (fileType.matcher(file.getName()).matches()) {
                        LOGGER.debug("Path : " + path);
                        findInFile(file, fileEntry, fileContent);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                    return FileVisitResult.SKIP_SUBTREE;
                }

            });
        } catch (final Exception e) {
            LOGGER.error(null, e);
        }
        LOGGER.warn("Found : " + ATOMIC_INTEGER.get() + " instances.");
    }

    private static void findInFile(final File file, final Pattern fileEntry, final Pattern fileContent) {
        if (file == null || !file.exists() || !file.canRead() || !file.isFile()) {
            return;
        }
        ZipFile zip = null;
        try {
            try {
                zip = new ZipFile(file);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage() + ", " + file);
            }
            if (zip == null) {
                return;
            }
            Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
            while (zipFileEntries.hasMoreElements()) {
                // Grab a zip file entry
                ZipEntry entry = zipFileEntries.nextElement();
                if (fileEntry.matcher(entry.getName()).matches()) {
                    if (fileContent == null) {
                        LOGGER.error("Found in file : " + file + ", " + fileEntry + ", " + entry.getName() + ", " + entry);
                        ATOMIC_INTEGER.incrementAndGet();
                    } else {
                        InputStream inputStream = zip.getInputStream(entry);
                        String contents = FILE.getContents(inputStream, Integer.MAX_VALUE).toString();
                        contents = STRING.stripToAlphaNumeric(contents);
                        if (fileContent.matcher(contents).matches()) {
                            LOGGER.error("In file : " + file + ", " + fileEntry + ", " + entry.getName() + ", " + entry);
                            LOGGER.error("Contents : " + contents);
                            ATOMIC_INTEGER.incrementAndGet();
                        }
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error(null, e);
        } finally {
            try {
                if (zip != null) {
                    zip.close();
                }
            } catch (final Exception e) {
                LOGGER.error(null, e);
            }
        }
    }

}