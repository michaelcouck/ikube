package ikube.index.handler.filesystem;

import ikube.index.IndexManager;
import ikube.index.handler.Handler;
import ikube.index.handler.IHandler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableFilesystemHandler extends Handler {

	public IndexableFilesystemHandler(IHandler<Indexable<?>> previous) {
		super(previous);
	}

	public List<Thread> handle(final IndexContext indexContext, final Indexable<?> indexable) throws Exception {
		if (IndexableFileSystem.class.isAssignableFrom(indexable.getClass())) {
			handleFilesystem(indexContext, (IndexableFileSystem) indexable);
		}
		// super.handle(indexContext, indexable);
		return new ArrayList<Thread>();
	}

	protected void handleFilesystem(final IndexContext indexContext, IndexableFileSystem indexableFileSystem) {
		try {
			File baseFile = new File(indexableFileSystem.getPath());
			Pattern pattern = getPattern(indexableFileSystem.getExcludedPattern());
			if (baseFile.isDirectory()) {
				visitFolder(indexContext, indexableFileSystem, baseFile, pattern);
			} else {
				visitFile(indexContext, indexableFileSystem, baseFile);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	protected void visitFolder(final IndexContext indexContext, IndexableFileSystem indexableFileSystem, File folder,
			Pattern excludedPattern) {
		if (folder == null || !folder.exists()) {
			logger.error("The folder could not be found : " + folder);
			return;
		}
		File[] files = folder.listFiles();
		if (files != null) {
			for (java.io.File file : files) {
				logger.debug("Visiting file : " + file);
				if (isExcluded(file, excludedPattern)) {
					continue;
				}
				if (file.isDirectory()) {
					visitFolder(indexContext, indexableFileSystem, file, excludedPattern);
				} else {
					if (!file.exists() || !file.canRead()) {
						logger.info("Skipping file : " + file.getAbsolutePath());
						continue;
					}
					visitFile(indexContext, indexableFileSystem, file);
				}
			}
		}
	}

	public void visitFile(final IndexContext indexContext, IndexableFileSystem indexableFileSystem, File file) {
		try {
			Document document = new Document();
			indexableFileSystem.setCurrentFile(file);

			// TODO - this can be very large so we have to use a reader if necessary
			InputStream inputStream = new ByteArrayInputStream(FileUtilities.getContents(file).toByteArray());

			byte[] bytes = new byte[1024];
			if (inputStream.markSupported()) {
				inputStream.mark(Integer.MAX_VALUE);
				inputStream.read(bytes);
				inputStream.reset();
			}

			IParser parser = ParserProvider.getParser(file.getName(), bytes);
			OutputStream parsedOutputStream = parser.parse(inputStream, new ByteArrayOutputStream());

			Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;

			IndexManager.addStringField(indexableFileSystem.getPathFieldName(), file.getAbsolutePath(), document, store, analyzed,
					termVector);
			IndexManager.addStringField(indexableFileSystem.getNameFieldName(), file.getName(), document, store, analyzed, termVector);
			IndexManager.addStringField(indexableFileSystem.getLastModifiedFieldName(), Long.toString(file.lastModified()), document,
					store, analyzed, termVector);
			IndexManager.addStringField(indexableFileSystem.getLengthFieldName(), Long.toString(file.length()), document, store, analyzed,
					termVector);
			IndexManager.addStringField(indexableFileSystem.getContentFieldName(), parsedOutputStream.toString(), document, store,
					analyzed, termVector);

			indexContext.getIndexWriter().addDocument(document);

		} catch (Exception e) {
			logger.error("Exception occured while trying to index the file " + file.getAbsolutePath(), e);
		}
	}

	protected Pattern getPattern(String pattern) {
		return Pattern.compile(pattern);
	}

	protected boolean isExcluded(File file, Pattern pattern) {
		return !file.canRead() || isVisited(file) || pattern.matcher(file.getName()).matches();
	}

	protected boolean isVisited(File file) {
		// TODO - check if visited in the cluster
		return false;
	}

}
