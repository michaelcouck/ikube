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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;

/**
 * This class will be called by the framework to index the resources
 * from the SVN provider, during the indexing of an SVN repository.
 *
 * Updated the parsers to the Tika framework from Apache.
 *
 * @author Michael Couck
 * @version 01.10
 * @since 04-06-2013
 */
public class SvnResourceHandler extends ResourceHandler<IndexableSvn> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Document handleResource(final IndexContext indexContext, final IndexableSvn indexableSvn, final Document document,
                                   final Object resource) throws Exception {
        SVNDirEntry dirEntry = (SVNDirEntry) resource;

        // Can't do this because it is cloned: indexableSvn.getRepository()
        SVNRepository repository = getSvnRepository(indexContext, indexableSvn.getName());
        SVNNodeKind svnNodeKind = dirEntry.getKind();

        SVNURL svnurl = dirEntry.getURL();
        String url = svnurl.toString();
        String repositoryRoot = dirEntry.getRepositoryRoot().toString();
        // Strip the repository root off the url to get the relative path to the resource
        String path = url.replace(repositoryRoot, "");

        logger.debug("Url : " + url + ", repository root : " + repositoryRoot + ", path : " + path);

        if (SVNNodeKind.FILE.equals(svnNodeKind)) {
            String name = dirEntry.getName();
            String author = dirEntry.getAuthor();
            String commit = dirEntry.getCommitMessage();
            Date date = dirEntry.getDate();
            long revision = dirEntry.getRevision();
            long size = dirEntry.getSize();

            SVNProperties fileProperties = SVNProperties.wrap(new HashMap<>());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // TODO: Search the index for the revision first, only index if it is changed
            repository.getFile(path, -1, fileProperties, byteArrayOutputStream);

            indexableSvn.setRawContent(byteArrayOutputStream.toByteArray());

            parseContent(indexableSvn, name);

            Object content = indexableSvn.getContent();
            if (content == null) {
                content = "";
            }
            logger.warn("Host : " + svnurl.getHost() +
                    ", path : " + svnurl.getPath() +
                    ", port : " + svnurl.getPort() +
                    ", kind : " + svnNodeKind +
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
            IndexManager.addStringField(indexableSvn.getContents(), content.toString(), indexableSvn, document);
        }

        return super.handleResource(indexContext, indexableSvn, document, resource);
    }

    protected void parseContent(final IndexableSvn indexableSvn, final String name) {
        try {
            byte[] buffer = (byte[]) indexableSvn.getRawContent();
            // String contentType;

            //MimeType mimeType = MimeTypes.getMimeType(name, buffer);
            //if (mimeType == null) {
            //    mimeType = new MimeType("text", "plain");
            //}

            // The first few bytes so we can guess the content type
            byte[] bytes = new byte[Math.min(buffer.length, 1024)];
            System.arraycopy(buffer, 0, bytes, 0, bytes.length);
            // IParser parser = ParserProvider.getParser(mimeType.getName(), bytes);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer, 0, buffer.length);
            //OutputStream outputStream = null;
            //try {
            //    outputStream = parser.parse(byteArrayInputStream, new ByteArrayOutputStream());
            //} catch (final Exception e) {
            //    // If this is an XML exception then try the HTML parser
            //    if (XMLParser.class.isAssignableFrom(parser.getClass())) {
            //        contentType = "text/html";
            //        parser = ParserProvider.getParser(contentType, bytes);
            //        outputStream = parser.parse(byteArrayInputStream, new ByteArrayOutputStream());
            //    } else {
            //        String message = "Exception parsing content from url : " + indexableSvn.getUrl();
            //        logger.error(message, e);
            //    }
            //}
            //if (outputStream != null) {
            //    String parsedContent = outputStream.toString();
            //    logger.debug("Parsed content length : " + parsedContent.length() + ", content type : " + mimeType);
            //    indexableSvn.setContent(parsedContent);
            //}

            //AutoDetectParser parser = new AutoDetectParser();
            //BodyContentHandler handler = new BodyContentHandler((int) indexableSvn.getMaxReadLength());
            //Metadata metadata = new Metadata();
            //
            //parser.parse(byteArrayInputStream, handler, metadata);
            String parsedContent = ""; // handler.toString();
            indexableSvn.setContent(parsedContent);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    SVNRepository getSvnRepository(final Indexable indexable, final String name) {
        if (indexable.getName().equals(name) && IndexableSvn.class.isAssignableFrom(indexable.getClass())) {
            return ((IndexableSvn) indexable).getRepository();
        }
        for (final Indexable child : indexable.getChildren()) {
            if (child.getName().equals(name) && IndexableSvn.class.isAssignableFrom(child.getClass())) {
                return getSvnRepository(child, name);
            }
        }
        throw new RuntimeException("No repository found for indexable : " + name);
    }

}