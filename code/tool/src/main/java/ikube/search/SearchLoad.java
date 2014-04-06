package ikube.search;

import com.google.gson.Gson;
import ikube.IConstants;
import ikube.model.Search;
import ikube.toolkit.Timer;
import ikube.web.service.SearcherJson;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 14-04-2012
 */
public class SearchLoad {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchLoad.class);

    public static void main(String[] args) throws IOException {
        Search search = new Search();
        search.setDistributed(Boolean.FALSE);
        search.setSearchStrings(Arrays.asList("cape town"));
        search.setFirstResult(0);
        search.setFragment(Boolean.TRUE);
        search.setIndexName(IConstants.GEOSPATIAL);
        search.setMaxResults(10);
        search.setOccurrenceFields(Arrays.asList("must"));
        search.setSearchFields(Arrays.asList(IConstants.NAME));
        search.setTypeFields(Arrays.asList("string"));

        String url = getUrl();

        final HttpClient httpClient = new HttpClient();
        final PostMethod postMethod = new PostMethod(url);

        Gson gson = new Gson();
        String content = gson.toJson(search);
        StringRequestEntity stringRequestEntity = new StringRequestEntity(content, MediaType.APPLICATION_JSON, IConstants.ENCODING);
        postMethod.setRequestEntity(stringRequestEntity);

        final int iterations = 10000;
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                try {
                    for (int i = 0; i < iterations; i++) {
                        httpClient.executeMethod(postMethod);
                    }
                } catch (final IOException e) {
                    LOGGER.error("Exception searching production server : ", e);
                }
            }
        });
        LOGGER.info("Duration for searching : " + iterations + ", is : " + duration + ", per second is : " + iterations / (duration / 1000));
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    protected static String getUrl() throws MalformedURLException {
        StringBuilder builder = new StringBuilder();
        builder.append(IConstants.SEP);
        builder.append(IConstants.IKUBE);
        builder.append("service");
        builder.append(SearcherJson.SEARCH);
        builder.append(SearcherJson.JSON);
        return new URL("http", "ikube.be", 80, builder.toString()).toString();
    }

}
