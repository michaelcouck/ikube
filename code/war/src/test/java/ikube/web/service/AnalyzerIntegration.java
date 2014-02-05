package ikube.web.service;

import com.google.gson.Gson;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.model.Context;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

@Ignore
public class AnalyzerIntegration extends BaseTest {

    @Test
    public void create() throws IOException {
        PostMethod postMethod = new PostMethod(getUrl(Analyzer.CREATE));
        Gson gson = new Gson();
        Context context = new Context();
        String content = gson.toJson(context);
        StringRequestEntity stringRequestEntity = new StringRequestEntity(content, MediaType.APPLICATION_JSON, IConstants.ENCODING);
        postMethod.setRequestEntity(stringRequestEntity);
        HTTP_CLIENT.executeMethod(postMethod);
        assertEquals(200, postMethod.getStatusCode());
    }

    @Test
    public void train() {
        fail();
    }

    @Test
    public void build() {
        fail();
    }

    @Test
    public void analyze() {
        fail();
    }

    @Test
    public void destroy() {
        fail();
    }

    @Test
    public void analyzers() {
        fail();
    }

    @Test
    public void context() {
        fail();
    }

    @Test
    public void contexts() {
        fail();
    }

    @Test
    public void newLineToLineBreak() {
        fail();
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    protected String getUrl(final String service) throws MalformedURLException {
        StringBuilder builder = new StringBuilder();
        builder.append(IConstants.SEP);
        builder.append(IConstants.IKUBE);
        builder.append(Analyzer.ANALYZER);
        builder.append(service);
        return new URL("http", LOCALHOST, SERVER_PORT, builder.toString()).toString();
    }

}