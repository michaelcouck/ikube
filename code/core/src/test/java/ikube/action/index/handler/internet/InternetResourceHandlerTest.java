package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.FileUtilities;
import junit.framework.Assert;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-06-2013
 */
public class InternetResourceHandlerTest extends AbstractTest {

    private InternetResourceHandler internetResourceHandler;

    @Before
    public void before() {
        internetResourceHandler = new InternetResourceHandler();
    }

    @Test
    public void handleResource() throws Exception {
        String uri = "http://www.ikokoon.com";
        IndexableInternet indexableInternet = mock(IndexableInternet.class);
        when(indexableInternet.getIdFieldName()).thenReturn(IConstants.ID);
        when(indexableInternet.getTitleFieldName()).thenReturn(IConstants.TITLE);
        when(indexableInternet.getContentFieldName()).thenReturn(IConstants.CONTENT);
        when(indexableInternet.isStored()).thenReturn(Boolean.TRUE);
        when(indexableInternet.isAnalyzed()).thenReturn(Boolean.TRUE);
        when(indexableInternet.isOmitNorms()).thenReturn(Boolean.FALSE);
        when(indexableInternet.isTokenized()).thenReturn(Boolean.FALSE);
        when(indexableInternet.isVectored()).thenReturn(Boolean.FALSE);

        Document document = new Document();

        File file = FileUtilities.findFileRecursively(new File("."), "html.html");
        String contents = FileUtilities.getContents(file, Integer.MAX_VALUE).toString();
        Url url = mock(Url.class);
        when(url.getUrl()).thenReturn(uri);
        when(url.getContentType()).thenReturn("text/html");

        when(url.getRawContent()).thenReturn(contents.getBytes());
        when(url.getParsedContent()).thenReturn(contents);
        internetResourceHandler.handleResource(indexContext, indexableInternet, document, url);

        Assert.assertEquals(uri, document.get(IConstants.ID));
        Assert.assertEquals("Ikokoon", document.get(IConstants.TITLE));
        Assert.assertTrue(document.get(IConstants.CONTENT).contains("What determines productivity, and how is it measured?"));

    }

}
