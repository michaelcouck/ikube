package ikube.data.wiki;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will read a Bzip2 file with a big xml in it, unpack the xml, parse it and write individual files to disk.
 * 
 * @author Michael Couck
 * @since at least 14.04.2012
 * @version 01.00
 */
public class WikiDataUnpackerWorker implements Runnable {

	/** This is the start and end tags for the xml data, one per page essentially. */
	public static final String PAGE_START = "<revision>";
	public static final String PAGE_FINISH = "</revision>";

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpackerWorker.class);

	/** The disk where this file is to be unpacked. */
	private File disk;

	/**
	 * Constructor takes the input file, i.e. the Bzip file with the xml data, and the disk where the file is to be unpacked.
	 * 
	 * @param disk the output disk
	 */
	public WikiDataUnpackerWorker(final File disk) {
		this.disk = disk;
	}

	/**
	 * This method will read the zip file, add the contents to the string builder then write it out to the file.
	 */
	@Override
	public void run() {
		List<File> bZip2Files = FileUtilities.findFilesRecursively(disk, new ArrayList<File>(), "bz2");
		// Sort them by the name
		Collections.sort(bZip2Files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		LOGGER.info("Files : " + bZip2Files);
		for (File bZip2File : bZip2Files) {
			File baseDirectory = new File(disk, FilenameUtils.removeExtension(bZip2File.getName()));
			Set<String> fileHashes = new TreeSet<String>();
			if (baseDirectory.exists() && baseDirectory.isDirectory()) {
				// Get all the files in the directory and add them to the set of names
				List<File> files = FileUtilities.findFilesRecursively(baseDirectory, new ArrayList<File>(), "html");
				for (File file : files) {
					fileHashes.add(file.getName());
				}
			}
			LOGGER.info("Doing file : " + bZip2File + ", on disk : " + disk);
			FileInputStream fileInputStream = null;
			BZip2CompressorInputStream bZip2CompressorInputStream = null;
			int totalCount = 0;
			try {
				int read = -1;
				int count = 0;
				File outputDirectory = getNextDirectory(bZip2File, System.currentTimeMillis());
				fileInputStream = new FileInputStream(bZip2File);
				bZip2CompressorInputStream = new BZip2CompressorInputStream(fileInputStream);
				byte[] bytes = new byte[1024 * 1024 * 8];
				StringBuilder stringBuilder = new StringBuilder();
				while ((read = bZip2CompressorInputStream.read(bytes)) > -1) {
					String string = new String(bytes, 0, read, Charset.forName(IConstants.ENCODING));
					stringBuilder.append(string);
					count += unpack(outputDirectory, stringBuilder, fileHashes);
					if (count > 10000) {
						outputDirectory = getNextDirectory(bZip2File, System.currentTimeMillis());
						LOGGER.info("Count : " + count + ", output directory : " + outputDirectory + ", total : " + (totalCount += count));
						count = 0;
					}
				}
			} catch (Exception e) {
				LOGGER.error("Exception reading and uncompressing the zip file : " + bZip2File + ", " + disk, e);
			} finally {
				FileUtilities.close(fileInputStream);
				FileUtilities.close(bZip2CompressorInputStream);
			}
		}
	}

	/**
	 * This method will get all the unpacked files in the directory and put the names in a set.
	 * 
	 * @param bZip2File the file that will be unpacked
	 * @return the set of file names that are already unpacked
	 */
	protected Set<String> getFileHashes(final File bZip2File) {
		File baseDirectory = new File(disk, FilenameUtils.removeExtension(bZip2File.getName()));
		Set<String> fileHashes = new TreeSet<String>();
		if (baseDirectory.exists() && baseDirectory.isDirectory()) {
			// Get all the files in the directory and add them to the set of names
			List<File> files = FileUtilities.findFilesRecursively(baseDirectory, new ArrayList<File>(), "html");
			for (File file : files) {
				if (!fileHashes.add(FilenameUtils.getBaseName(file.getName()))) {
					LOGGER.warn("Already contained : " + file.getName());
				}
			}
		}
		return fileHashes;
	}

	/**
	 * This method will take the input data(the string builder) and parse it, breaking the xml into 'pages' between the start and end tags,
	 * then write this 'segment' to individual files.
	 * 
	 * @param outputDirectory the directory where to write the output files
	 * @param stringBuilder the string of xml data to parse and write
	 * @return the number of files written to the disk
	 * @throws Exception
	 */
	protected int unpack(final File outputDirectory, final StringBuilder stringBuilder, final Set<String> fileHashes) throws Exception {
		int count = 0;
		while (true) {
			int startOffset = stringBuilder.indexOf(PAGE_START);
			int endOffset = stringBuilder.indexOf(PAGE_FINISH);
			if (startOffset == -1 || endOffset == -1) {
				break;
			}
			if (endOffset <= startOffset) {
				startOffset = endOffset;
			}
			endOffset += PAGE_FINISH.length();
			String segment = stringBuilder.substring(startOffset, endOffset);
			stringBuilder.delete(startOffset, endOffset);
			String hash = Long.toString(HashUtilities.hash(segment));
			// Check if this file is written already
			if (fileHashes.contains(hash)) {
				continue;
			}
			String filePath = outputDirectory.getAbsolutePath() + File.separator + hash + ".html";
			FileUtilities.setContents(filePath, segment.getBytes(Charset.forName(IConstants.ENCODING)));
			count++;
		}
		return count;
	}

	private File getNextDirectory(final File bZip2File, final long fileName) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(disk.getAbsolutePath());
		stringBuilder.append(File.separator);
		stringBuilder.append(FilenameUtils.removeExtension(bZip2File.getName()));
		stringBuilder.append(File.separator);
		stringBuilder.append(fileName);
		return FileUtilities.getFile(stringBuilder.toString(), Boolean.TRUE);
	}

}