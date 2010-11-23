package ikube.index.visitor.filesystem;

import ikube.index.content.FileContentProvider;
import ikube.index.content.IContentProvider;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.index.visitor.IndexableVisitor;
import ikube.model.IndexableFileSystem;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * TODO - when the file is visited then post to the cluster. Only one server will index the files.
 *
 * @author Cristi Bozga
 * @since 05.11.10
 * @version 01.00
 */
public class IndexableFileSystemVisitor<I> extends IndexableVisitor<IndexableFileSystem> {

	private Pattern pattern;
	private IContentProvider<IndexableFileSystem> contentProvider;

	public IndexableFileSystemVisitor() {
		this.contentProvider = new FileContentProvider();
	}

	@Override
	public void visit(IndexableFileSystem indexableFileSystem) {
		try {
			File file = new File(indexableFileSystem.getPath());
			if (!file.exists()) {
				logger.error("FileConfiguration:" + indexableFileSystem.getName() + ",  the file " + indexableFileSystem.getPath()
						+ " could not be found;");
				return;
			}
			Pattern pattern = getPattern(indexableFileSystem.getExcludedPattern());
			boolean isExcluded = isExcluded(file, pattern);
			if (isExcluded) {
				return;
			}
			if (file.isDirectory()) {
				visitFolder(indexableFileSystem, file);
			} else {
				visitFile(indexableFileSystem, file);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	protected void visitFolder(IndexableFileSystem indexableFileSystem, File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (java.io.File file : files) {
				logger.debug("Visiting file : " + file);
				if (isExcluded(file, pattern)) {
					continue;
				}
				if (file.isDirectory()) {
					visitFolder(indexableFileSystem, file);
				} else {
					visitFile(indexableFileSystem, file);
				}
			}
		}
	}

	public void visitFile(IndexableFileSystem indexableFileSystem, File file) {
		try {
			Document document = new Document();

			// TODO - this can be very large so we have to use a reader if necessary
			InputStream inputStream = (InputStream) contentProvider.getContent(indexableFileSystem);

			byte[] bytes = new byte[1024];
			if (inputStream.markSupported()) {
				inputStream.mark(Integer.MAX_VALUE);
				inputStream.read(bytes);
				inputStream.reset();
			}

			IParser parser = ParserProvider.getParser(file.getName(), bytes);
			OutputStream parsedOutputStream = parser.parse(inputStream);

			Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;

			addStringField(indexableFileSystem.getPathFieldName(), file.getAbsolutePath(), document, store, analyzed, termVector);
			addStringField(indexableFileSystem.getNameFieldName(), file.getName(), document, store, analyzed, termVector);
			addStringField(indexableFileSystem.getLastModifiedFieldName(), Long.toString(file.lastModified()), document, store, analyzed,
					termVector);
			addStringField(indexableFileSystem.getLengthFieldName(), Long.toString(file.length()), document, store, analyzed, termVector);
			addStringField(indexableFileSystem.getContentFieldName(), parsedOutputStream.toString(), document, store, analyzed, termVector);

			getIndexContext().getIndexWriter().addDocument(document);

		} catch (Exception e) {
			logger.error("Exception occured while trying to index the file " + file.getAbsolutePath(), e);
		}
	}

	protected Pattern getPattern(String pattern) {
		if (this.pattern == null) {
			this.pattern = Pattern.compile(pattern);
		}
		return this.pattern;
	}

	protected boolean isExcluded(File file, Pattern pattern) {
		return !file.canRead() || isVisited(file) || pattern.matcher(file.getName()).matches();
	}

	protected boolean isVisited(File file) {
		// TODO - check if visited in the cluster
		return false;
	}

}