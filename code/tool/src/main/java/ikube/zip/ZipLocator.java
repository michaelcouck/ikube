package ikube.zip;

import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipLocator {

	public static void main(String[] args) {
		if (args == null || args.length < 3) {
			System.out.println("Usage : ikube.jar [path/to/folder] [zip-patterns] > example : >java -jar ikube.jar MyClassNameNoDots /tmp .zip .jar");
			return;
		}
		String path = args[1];
		List<File> files = new ArrayList<File>();
		for (int i = 2; i < args.length; i++) {
			String pattern = args[i];
			FileUtilities.findFilesRecursively(new File(path), files, pattern);
		}
		Pattern pattern = Pattern.compile(".*(" + args[0] + ").*");
		for (final File file : files) {
			if (file == null || !file.exists() || !file.canRead() || !file.isFile()) {
				continue;
			}
			ZipFile zip;
			try {
				zip = new ZipFile(file);
			} catch (Exception e) {
				// e.printStackTrace();
				// System.err.println(file);
				continue;
			}
			try {
				Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
				while (zipFileEntries.hasMoreElements()) {
					// Grab a zip file entry
					ZipEntry entry = zipFileEntries.nextElement();
					if (pattern.matcher(entry.getName()).matches()) {
						System.err.println("In file : " + file + ", " + pattern + ", " + entry.getName() + ", " + entry);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					zip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
