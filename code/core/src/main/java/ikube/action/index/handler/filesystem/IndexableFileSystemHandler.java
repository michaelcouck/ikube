package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlichtherle.truezip.file.TFile;

public class IndexableFileSystemHandler extends IndexableHandler<IndexableFileSystem> {

	@Autowired
	private FileResourceHandler resourceHandler;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ForkJoinTask<?> handleIndexableForked(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem) throws Exception {
		Pattern pattern = getPattern(indexableFileSystem.getExcludedPattern());
		IResourceProvider<File> fileSystemResourceProvider = new FileSystemResourceProvider(indexableFileSystem, pattern);
		return getRecursiveAction(indexContext, indexableFileSystem, fileSystemResourceProvider);
	}

	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final Object resource) {
		handleFile(indexContext, indexableFileSystem, (File) resource);
		return null;
	}

	/**
	 * As the name suggests this method accesses a file, and hopefully indexes the data adding it to the index.
	 * 
	 * @param indexContext the context for this index
	 * @param indexableFileSystem the file system object for storing data during the indexing
	 * @param file the file to parse and index
	 */
	void handleFile(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final File file) {
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
					for (final File innerTFile : tFiles) {
						if (FileUtilities.isExcluded(innerTFile, pattern)) {
							continue;
						}
						handleFile(indexContext, indexableFileSystem, innerTFile);
					}
				}
			}
		}
		// And now the files
		if (file.isDirectory()) {
			for (final File innerFile : file.listFiles()) {
				handleFile(indexContext, indexableFileSystem, innerFile);
			}
		} else {
			try {
				resourceHandler.handleResource(indexContext, indexableFileSystem, new Document(), file);
			} catch (Exception e) {
				handleException(indexableFileSystem, e, "Exception handling file : " + file);
			}
		}
	}

	@SuppressWarnings("unused")
	private boolean handleGZip(final String filePath) throws ArchiveException, IOException {
		InputStream is = new FileInputStream(filePath);
		ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream("tar", new GZIPInputStream(is));
		ArchiveEntry entry = null;
		while ((entry = input.getNextEntry()) != null) {
			// TODO implement this logic for tar on Linux
			logger.info("G-zip entry : " + entry);
		}
		return Boolean.TRUE;
	}

	protected synchronized Pattern getPattern(final String pattern) {
		return Pattern.compile(pattern != null ? pattern : "");
	}

}