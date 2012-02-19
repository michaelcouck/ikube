package ikube.data;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.Logging;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiDataUnpacker {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpacker.class);

	private static final String PAGE_START = "<page>";
	private static final String PAGE_FINISH = "</page>";
	private static final String wikiDataPath = "/usr/local/wiki/data/";

	/**
	 * This method will read from the xml Wiki data file and unpack it to directories.
	 */
	public static void main(String[] args) {
		Logging.configure();
		File outputDirectory = null;
		File file = new File(wikiDataPath, "enwiki-latest-pages-articles.xml");
		FileInputStream fileInputStream = null;
		try {
			int read = -1;
			int count = 0;
			ByteBuffer bytes = ByteBuffer.allocate(1024 * 1024);
			StringBuilder builder = new StringBuilder();
			fileInputStream = new FileInputStream(file);
			FileChannel fileChannel = fileInputStream.getChannel();
			File[] directories = new File(wikiDataPath).listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});
			if (directories != null && directories.length >= 1) {
				long position = 0;
				for (File directory : directories) {
					try {
						long directoryPosition = Long.parseLong(directory.getName());
						if (directoryPosition > position) {
							position = directoryPosition;
						}
					} catch (Exception e) {
						LOGGER.error(null, e);
					}
				}
				if (position > 0) {
					fileChannel.position(position);
				}
			}
			System.out.println("Position : " + fileChannel.position());
			// System.out.println(Arrays.toString(directories));
			while ((read = fileChannel.read(bytes)) > -1) {
				bytes.flip();
				String string = new String(bytes.array(), 0, read, Charset.forName(IConstants.ENCODING));
				builder.append(string);
				while (true) {
					int startOffset = builder.indexOf(PAGE_START);
					int endOffset = builder.indexOf(PAGE_FINISH);
					if (startOffset == -1 || endOffset == -1) {
						break;
					}
					if (endOffset <= startOffset) {
						startOffset = endOffset;
					}
					endOffset += PAGE_FINISH.length();
					String segment = builder.substring(startOffset, endOffset);
					builder.delete(startOffset, endOffset);
					String hash = Long.toString(HashUtilities.hash(segment));
					if (outputDirectory == null || count % 10000 == 0) {
						System.out.println("Count : " + count + ", position : " + fileChannel.position());
						outputDirectory = new File(wikiDataPath + fileChannel.position());
					}
					String filePath = outputDirectory.getAbsolutePath() + File.separator + hash + ".html";
					FileUtilities.setContents(filePath, segment.getBytes(Charset.forName(IConstants.ENCODING)));
					count++;
				}
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("No file found : ", e);
		} catch (IOException e) {
			LOGGER.error("IO exception : ", e);
		} finally {
			FileUtilities.close(fileInputStream);
		}
	}

	public static void main() throws Exception {
		Logging.configure();
		File wikiDataFolder = new File(wikiDataPath);
		File[] folders = wikiDataFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() && pathname.exists();
			}
		});
		for (File folder : folders) {
			LOGGER.info("Doing folder : " + folder);
			File[] files = folder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isFile() && pathname.exists();
				}
			});
			if (files != null && files.length > 0) {
				File outputFolder = null;
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if (i % 1000 == 0) {
						outputFolder = new File(folder, Integer.toString(i));
						if (!outputFolder.exists()) {
							outputFolder.mkdirs();
						}
						LOGGER.info("Output folder : " + outputFolder);
					}
					File renamedFile = new File(outputFolder, file.getName());
					boolean renamed = file.renameTo(renamedFile);
					if (!renamed) {
						LOGGER.warn("Couldn't rename : " + renamedFile);
					}
				}
			}
		}
	}

}
