package ikube.action.index.content;

import ikube.model.IndexableInternet;
import ikube.toolkit.FILE;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class allows writing the data returned from a url to the output stream specified in the parameter list.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 23-11-10
 */
@Deprecated
public class InternetContentProvider implements IContentProvider<IndexableInternet> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void getContent(final IndexableInternet indexableInternet, final OutputStream outputStream) {
        try {
            InputStream inputStream = indexableInternet.getCurrentInputStream();
            FILE.getContents(inputStream, outputStream, indexableInternet.getMaxReadLength());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}