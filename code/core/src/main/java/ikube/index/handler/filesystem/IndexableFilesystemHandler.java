package ikube.index.handler.filesystem;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.index.handler.IndexableHandler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;

/**
 * This class indexes a file share on the network. It is multi-threaded but not cluster load balanced.
 * 
 * This class is optimized for performance, as such it is not very elegant.
 * 
 * @author Cristi Bozga
 * @author Michael Couck
 * @since 29.11.10
 * @version 02.00<br>
 *          Updated this class to persist the files and to be multi threaded to improve the performance.
 */
public class IndexableFilesystemHandler extends IndexableHandler<IndexableFileSystem> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handle(final IndexContext<?> indexContext, final IndexableFileSystem indexable) throws Exception {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		final Stack<File> directories = new Stack<File>();
		directories.push(new File(indexable.getPath()));
		final Pattern pattern = getPattern(indexable.getExcludedPattern());
		for (int i = 0; i < getThreads(); i++) {
			final IndexableFileSystem indexableFileSystem = (IndexableFileSystem) SerializationUtilities.clone(indexable);
			// Must set the strategies because they are transient and will not be included in the clone
			indexableFileSystem.setParent(indexable.getParent());
			indexableFileSystem.setStrategies(indexable.getStrategies());
			Runnable runnable = new Runnable() {
				public void run() {
					handleFiles(indexContext, indexableFileSystem, directories, pattern);
				}
			};
			Future<?> future = ThreadUtilities.submit(indexContext.getIndexName(), runnable);
			futures.add(future);
		}

		return futures;
	}

	void handleFiles(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final Stack<File> directories,
			final Pattern pattern) {
		List<File> files = getBatch(indexableFileSystem, directories, pattern);
		do {
			for (File file : files) {
				try {
					// logger.error("Doing file : " + file);
					handleFile(indexContext, indexableFileSystem, file);
				} catch (InterruptedException e) {
					logger.error("Thread terminated, and indexing stopped : ", e);
					throw new RuntimeException(e);
				} catch (Exception e) {
					logger.error("Exception handling file : " + file, e);
					handleMaxExceptions(indexableFileSystem, e);
				}
			}
			files = getBatch(indexableFileSystem, directories, pattern);
		} while (files.size() > 0 && !Thread.currentThread().isInterrupted());
	}

	/**
	 * As the name suggests this method accesses a file, and hopefully indexes the data adding it to the index.
	 * 
	 * @param indexContext the context for this index
	 * @param indexableFileSystem the file system object for storing data during the indexing
	 * @param file the file to parse and index
	 * @throws InterruptedException
	 */
	public void handleFile(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final File file)
			throws Exception {
		// First we handle the zips if necessary
		if (indexableFileSystem.isUnpackZips()) {
			boolean isFile = file.isFile();
			boolean isZip = IConstants.ZIP_JAR_WAR_EAR_PATTERN.matcher(file.getName()).matches();
			boolean isTrueFile = TFile.class.isAssignableFrom(file.getClass());
			boolean isZipAndFile = isZip && (isFile || isTrueFile);
			if (isZipAndFile) {
				TFile trueZipFile = new TFile(file);
				TFile[] tFiles = trueZipFile.listFiles();
				if (tFiles != null) {
					Pattern pattern = getPattern(indexableFileSystem.getExcludedPattern());
					for (File innerTFile : tFiles) {
						if (isExcluded(innerTFile, pattern)) {
							continue;
						}
						handleFile(indexContext, indexableFileSystem, innerTFile);
					}
				}
			}
		}
		// And noow the files
		if (file.isDirectory()) {
			for (File innerFile : file.listFiles()) {
				handleFile(indexContext, indexableFileSystem, innerFile);
			}
		} else {
			InputStream inputStream = null;
			try {
				if (TFile.class.isAssignableFrom(file.getClass())) {
					inputStream = new TFileInputStream(file);
				} else {
					inputStream = new FileInputStream(file);
				}
				Document document = new Document();
				addDocumentToIndex(indexContext, indexableFileSystem, file, inputStream, document);
			} finally {
				FileUtilities.close(inputStream);
			}
		}
	}

	protected List<File> getBatch(final IndexableFileSystem indexableFileSystem, final Stack<File> directories, final Pattern pattern) {
		List<File> fileBatch = new ArrayList<File>();
		if (!directories.isEmpty()) {
			File directory = directories.pop();
			if (directory.isFile()) {
				fileBatch.add(directory);
			}
			// Get the files for our directory that we are going to index
			File[] files = directory.listFiles();
			if (files != null && files.length > 0) {
				for (File file : files) {
					if (isExcluded(file, pattern)) {
						continue;
					}
					if (file.isDirectory()) {
						// Put all the directories on the stack
						directories.push(file);
					} else {
						if (file.isFile() && file.canRead()) {
							// We'll do this file ourselves
							fileBatch.add(file);
						}
					}
				}
			}
			if (fileBatch.isEmpty()) {
				// Means that there were no files in this directory
				return getBatch(indexableFileSystem, directories, pattern);
			}
		}
		return fileBatch;
	}

	protected void addDocumentToIndex(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final File file,
			final InputStream inputStream, final Document document) throws Exception {
		ByteArrayInputStream byteInputStream = null;
		ByteArrayOutputStream byteOutputStream = null;
		try {
			int length = file.length() > 0 && file.length() < indexableFileSystem.getMaxReadLength() ? (int) file.length()
					: (int) indexableFileSystem.getMaxReadLength();
			byte[] byteBuffer = new byte[length];
			int read = inputStream.read(byteBuffer, 0, byteBuffer.length);

			byteInputStream = new ByteArrayInputStream(byteBuffer, 0, read);
			byteOutputStream = new ByteArrayOutputStream();

			IParser parser = ParserProvider.getParser(file.getName(), byteBuffer);
			String parsedContent = parser.parse(byteInputStream, byteOutputStream).toString();

			Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;

			// This is the unique id of the resource to be able to delete it
			String fileId = HashUtilities.hash(file.getAbsolutePath()).toString();
			String pathFieldName = indexableFileSystem.getPathFieldName();
			String nameFieldName = indexableFileSystem.getNameFieldName();
			String modifiedFieldName = indexableFileSystem.getLastModifiedFieldName();
			String lengthFieldName = indexableFileSystem.getLengthFieldName();
			String contentFieldName = indexableFileSystem.getContentFieldName();

			// NOTE to self: To be able to delete using the index writer the identifier field must be non analyzed and non
			// tokenized/vectored!
			IndexManager.addStringField(IConstants.FILE_ID, fileId, document, Store.YES, Index.NOT_ANALYZED, TermVector.NO);
			IndexManager.addStringField(pathFieldName, file.getAbsolutePath(), document, Store.YES, analyzed, termVector);
			IndexManager.addStringField(nameFieldName, file.getName(), document, Store.YES, analyzed, termVector);
			IndexManager.addStringField(modifiedFieldName, Long.toString(file.lastModified()), document, Store.YES, analyzed, termVector);
			IndexManager.addStringField(lengthFieldName, Long.toString(file.length()), document, Store.YES, analyzed, termVector);
			IndexManager.addStringField(contentFieldName, parsedContent, document, store, analyzed, termVector);
			addDocument(indexContext, indexableFileSystem, document);

			Thread.sleep(indexContext.getThrottle());
		} finally {
			FileUtilities.close(byteInputStream);
			FileUtilities.close(byteOutputStream);
		}
	}

	@SuppressWarnings("unused")
	private boolean handleGZip(final String filePath) throws ArchiveException, IOException {
		InputStream is = new FileInputStream(filePath);
		ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream("tar", new GZIPInputStream(is));
		ArchiveEntry entry;
		while ((entry = input.getNextEntry()) != null) {
		}
		return Boolean.TRUE;
	}

	/**
	 * This method checks to see if the file can be read, that it exists and that it is not in the excluded pattern defined in the
	 * configuration.
	 * 
	 * @param file the file to check for inclusion in the processing
	 * @param pattern the pattern that excludes explicitly files and folders
	 * @return whether this file is included and can be processed
	 */
	protected synchronized boolean isExcluded(final File file, final Pattern pattern) {
		// If it does not exist, we can't read it or directory excluded with the pattern
		if (file == null) {
			return Boolean.TRUE;
		}
		if (!file.exists() || !file.canRead()) {
			return Boolean.TRUE;
		}
		if (file.getName() == null || file.getAbsolutePath() == null) {
			return Boolean.TRUE;
		}
		String name = file.getName();
		String path = file.getAbsolutePath();
		boolean isNameExcluded = pattern.matcher(name).matches();
		boolean isPathExcluded = pattern.matcher(path).matches();
		boolean isSymLink = Boolean.TRUE;
		try {
			isSymLink = FileUtils.isSymlink(file);
			// isSymLink = !file.getAbsolutePath().equals(file.getCanonicalPath());
		} catch (IOException e) {
			logger.error("Exception checking sym link : " + file);
		}
		boolean isExcluded = isNameExcluded || isPathExcluded || isSymLink;
		// logger.error("Excluded : " + isExcluded + ", " + isNameExcluded + ", " + isPathExcluded + ", " + isSymLink + ", " + name + ", "
		// + path + ", " + pattern);
		return isExcluded;
	}

	protected synchronized Pattern getPattern(final String pattern) {
		return Pattern.compile(pattern != null ? pattern : "");
	}

}