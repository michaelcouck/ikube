package ikube.index.visitor.filesystem;

import ikube.index.visitor.IndexableVisitor;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * Not tested or properly implemented.
 *
 * @author Cristi Bozga
 * @since 05.11.10
 * @version 01.00
 */
public class IndexableFileSystemVisitor<I> extends IndexableVisitor<IndexableFileSystem> {

	@Override
	public void visit(IndexableFileSystem indexableFileSystem) {
		try {
			File file = new File(indexableFileSystem.getPath());
			if (!file.exists()) {
				logger.error("FileConfiguration:" + indexableFileSystem.getName() + ",  the file " + indexableFileSystem.getPath()
						+ " could not be found;");
				return;
			}
			if (isExcluded(file, getPattern(indexableFileSystem.getExcludedPattern()))) {
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
		File[] children = folder.listFiles();
		if (children != null) {
			for (java.io.File file : children) {
				visitFile(indexableFileSystem, file);
			}
		}
	}

	public void visitFile(IndexableFileSystem indexableFileSystem, File file) {
		try {
			Document document = new Document();

			// TODO - this can be very large so we have t use a reader if necessary
			String fileContent = FileUtilities.getContents(file).toString();
			Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;

			addStringField(indexableFileSystem.getPathFieldName(), file.getAbsolutePath(), document, store, analyzed, termVector);
			addStringField(indexableFileSystem.getNameFieldName(), file.getName(), document, store, analyzed, termVector);
			addStringField(indexableFileSystem.getLastModifiedFieldName(), Long.toString(file.lastModified()), document, store, analyzed,
					termVector);
			addStringField(indexableFileSystem.getLengthFieldName(), Long.toString(file.length()), document, store, analyzed, termVector);
			addStringField(indexableFileSystem.getContentFieldName(), fileContent, document, store, analyzed, termVector);

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