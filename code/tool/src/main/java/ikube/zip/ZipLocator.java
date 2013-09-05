package ikube.zip;

import ikube.toolkit.FileUtilities;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will scan zip files and look for entries that are specified by name.
 * 
 * @author Michael Couck
 * @since 08.02.2011
 * @version 01.00
 */
public class ZipLocator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZipLocator.class);

	static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

	public static void main(String[] args) {
		if (args == null || args.length < 3) {
			System.out.println("Usage : java -jar ikube-xxx....jar [path/to/folder] [zip-patterns] [file pattern] [contents pattern]");
			System.out
					.println("example : java -jar ikube-tool-4.4.0.jar /tmp .*(\\.zip\\Z).*|.*(\\.jar\\Z).*|.*(\\.war\\Z).*|.*(\\.ear\\Z).* SerenityPublisher Hello World");
			return;
		}
		final String path = args[0];
		final Pattern fileType = Pattern.compile(args[1]);
		final Pattern fileEntry = Pattern.compile(".*(" + args[2] + ").*");
		final Pattern fileContent = args.length > 3 ? Pattern.compile(".*(" + args[3] + ").*") : null;
		try {
			Files.walkFileTree(new File(path).toPath(), new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
					File file = path.toFile();
					if (fileType.matcher(file.getName()).matches()) {
						findInFile(file, fileEntry, fileContent);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.SKIP_SUBTREE;
				}

			});
		} catch (Exception e) {
			LOGGER.error(null, e);
		}
		LOGGER.info("Found : " + ATOMIC_INTEGER.get() + " instances.");
	}

	private static void findInFile(final File file, final Pattern fileEntry, final Pattern fileContent) {
		if (file == null || !file.exists() || !file.canRead() || !file.isFile()) {
			return;
		}
		ZipFile zip = null;
		try {
			try {
				zip = new ZipFile(file);
			} catch (Exception e) {
				// LOGGER.error(e.getMessage() + ", " + file);
			}
			if (zip != null) {
				Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
				while (zipFileEntries.hasMoreElements()) {
					// Grab a zip file entry
					ZipEntry entry = zipFileEntries.nextElement();
					if (fileEntry.matcher(entry.getName()).matches()) {
						if (fileContent == null) {
							LOGGER.info("In file : " + file + ", " + fileEntry + ", " + entry.getName() + ", " + entry);
						} else {
							InputStream inputStream = zip.getInputStream(entry);
							String contents = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
							contents = stripToAlphaNumeric(contents);
							if (fileContent.matcher(contents).matches()) {
								LOGGER.info("In file : " + file + ", " + fileEntry + ", " + entry.getName() + ", " + entry);
								LOGGER.info("Contents : " + contents);
								ATOMIC_INTEGER.incrementAndGet();
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(null, e);
		} finally {
			try {
				if (zip != null) {
					zip.close();
				}
			} catch (Exception e) {
				LOGGER.error(null, e);
			}
		}
	}

	public static final String stripToAlphaNumeric(final String string) {
		char[] chars = string.toCharArray();
		char[] strippedChars = new char[chars.length];
		int j = 0;
		for (int i = 0; i < chars.length; i++) {
			final char c = chars[i];
			if (!Character.isLetterOrDigit(c)) {
				if (j != 0 && strippedChars[j - 1] != ' ') {
					strippedChars[j] = ' ';
				}
				continue;
			}
			strippedChars[j] = c;
			j++;
		}
		return new String(strippedChars, 0, j);
	}

}