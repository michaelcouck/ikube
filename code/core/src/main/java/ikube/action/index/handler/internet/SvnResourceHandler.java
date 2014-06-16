package ikube.action.index.handler.internet;

import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableSvn;
import org.apache.lucene.document.Document;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
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
        SVNRepository repository = getSvnRepository(indexContext, indexableSvn.getName());
        SVNNodeKind svnNodeKind = dirEntry.getKind();

        SVNURL svnurl = dirEntry.getURL();
        String url = svnurl.toString();
        String repositoryRoot = dirEntry.getRepositoryRoot().toString();
        // Strip the repository root off the url to get the relative path to the resource
        String path = url.replace(repositoryRoot, "");

        logger.info("Url : " + url + ", repository root : " + repositoryRoot + ", path : " + path);
        logger.info("Host : " + svnurl.getHost() +
                ", path : " + svnurl.getPath() +
                ", port : " + svnurl.getPort());

        if (SVNNodeKind.FILE.equals(svnNodeKind)) {
            String author = dirEntry.getAuthor();
            String commit = dirEntry.getCommitMessage();
            Date date = dirEntry.getDate();
            String name = dirEntry.getName();
            long revision = dirEntry.getRevision();
            long size = dirEntry.getSize();

            SVNProperties fileProperties = SVNProperties.wrap(new HashMap<>());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            repository.getFile(path, -1, fileProperties, byteArrayOutputStream);

            String contents = new String(byteArrayOutputStream.toByteArray());
            logger.info("Kind : " + svnNodeKind +
                    ", author : " + author +
                    ", commit : " + commit +
                    ", date : " + date +
                    ", name : " + name +
                    ", revision : " + revision +
                    ", size : " + size);

            IndexManager.addStringField(indexableSvn.getResourceName(), name, indexableSvn, document);
            IndexManager.addStringField(indexableSvn.getAuthor(), author, indexableSvn, document);
            IndexManager.addStringField(indexableSvn.getCommitComment(), commit, indexableSvn, document);
            IndexManager.addStringField(indexableSvn.getRelativeFilePath(), dirEntry.getRelativePath(), indexableSvn, document);
            IndexManager.addNumericField(indexableSvn.getRevision(), Long.toString(revision), document, Boolean.TRUE, indexableSvn.getBoost());
            IndexManager.addNumericField(indexableSvn.getRevisionDate(), Long.toString(date.getTime()), document, Boolean.TRUE, indexableSvn.getBoost());
            IndexManager.addNumericField(indexableSvn.getSize(), Long.toString(size), document, Boolean.TRUE, indexableSvn.getBoost());
            IndexManager.addStringField(indexableSvn.getContents(), contents, indexableSvn, document);
        }

        return super.handleResource(indexContext, indexableSvn, document, resource);
    }

    private SVNRepository getSvnRepository(final Indexable indexable, final String name) {
        for (final Indexable child : indexable.getChildren()) {
            if (child.getName().equals(name) && IndexableSvn.class.isAssignableFrom(child.getClass())) {
                return ((IndexableSvn) child).getRepository();
            }
        }
        throw new RuntimeException("No repository found for indexable : " + name);
    }

}