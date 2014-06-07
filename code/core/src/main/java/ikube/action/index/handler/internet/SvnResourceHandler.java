package ikube.action.index.handler.internet;

import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableSvn;
import org.apache.lucene.document.Document;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 04-06-2013
 */
public class SvnResourceHandler extends ResourceHandler<IndexableSvn> {

	/**
	 * {@inheritDoc}
	 *
	 * @throws Exception
	 */
	@Override
	public Document handleResource(
			final IndexContext indexContext,
			final IndexableSvn indexableSvn,
			final Document document,
			final Object resource) throws Exception {
		SVNDirEntry dirEntry = (SVNDirEntry) resource;
		SVNRepository repository = indexableSvn.getRepository();
		String filePath = indexableSvn.getFilePath();
		String relativePath = dirEntry.getRelativePath();
		String childFilePath = filePath + "/" + relativePath;
		SVNNodeKind svnNodeKind = dirEntry.getKind();

		if (SVNNodeKind.FILE.equals(svnNodeKind)) {
			SVNProperties fileProperties = SVNProperties.wrap(new HashMap<>());
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			repository.getFile(childFilePath, -1, fileProperties, byteArrayOutputStream);
			String author = dirEntry.getAuthor();
			String commit = dirEntry.getCommitMessage();
			Date date = dirEntry.getDate();
			String name = dirEntry.getName();
			long revision = dirEntry.getRevision();
			long size = dirEntry.getSize();
			String content = new String(byteArrayOutputStream.toByteArray());
			logger.info(svnNodeKind + ":" + author + ":" + commit + ":" + date + ":" + name + ":" + revision + ":" + size);

			IndexManager.addStringField(indexableSvn.getResourceName(), name, indexableSvn, document);
			IndexManager.addStringField(indexableSvn.getAuthor(), author, indexableSvn, document);
			IndexManager.addStringField(indexableSvn.getCommitComment(), commit, indexableSvn, document);
			IndexManager.addStringField(indexableSvn.getRelativeFilePath(), relativePath, indexableSvn, document);
			IndexManager.addNumericField(indexableSvn.getRevision(), Long.toString(revision), document, Boolean.TRUE, indexableSvn.getBoost());
			IndexManager.addNumericField(indexableSvn.getRevisionDate(), Long.toString(date.getTime()), document, Boolean.TRUE, indexableSvn.getBoost());
			IndexManager.addNumericField(indexableSvn.getSize(), Long.toString(size), document, Boolean.TRUE, indexableSvn.getBoost());
			IndexManager.addStringField(indexableSvn.getContent().toString(), content, indexableSvn, document);
		}

		return super.handleResource(indexContext, indexableSvn, document, resource);
	}

}