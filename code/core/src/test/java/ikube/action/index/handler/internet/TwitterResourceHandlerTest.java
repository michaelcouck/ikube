package ikube.action.index.handler.internet;

import au.com.bytecode.opencsv.CSVReader;
import com.google.gson.GsonBuilder;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Coordinate;
import ikube.model.IndexableTweets;
import ikube.model.Search;
import ikube.toolkit.FileUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.social.twitter.api.Tweet;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 19-06-2013
 */
public class TwitterResourceHandlerTest extends AbstractTest {

    private TwitterResourceHandler twitterResourceHandler;

    @Before
    @SuppressWarnings("unchecked")
    public void before() {
        twitterResourceHandler = new TwitterResourceHandler();
        twitterResourceHandler.init();
    }

    @Test
    public void handleResource() throws Exception {
        File file = FileUtilities.findFileRecursively(new File("."), "tweet.json");
        String json = FileUtilities.getContent(file);
        Tweet tweet = new GsonBuilder().disableHtmlEscaping().create().fromJson(json, Tweet.class);
        Document document = new Document();
        IndexableTweets indexableTweets = mock(IndexableTweets.class);

        when(indexableTweets.getCreatedAtField()).thenReturn("created-at");
        when(indexableTweets.getFromUserField()).thenReturn("from-user");
        when(indexableTweets.getTextField()).thenReturn("text-field");
        when(indexableTweets.getUserNameField()).thenReturn("user-name");
        when(indexableTweets.getUserLocationField()).thenReturn("user-location");
        when(indexableTweets.getUserLanguageField()).thenReturn("user-language");
        when(indexableTweets.getContent()).thenReturn("tweet-content");
        when(indexableTweets.getUserUtcOffsetField()).thenReturn("tweet-utc-offset");
        when(indexableTweets.isStored()).thenReturn(Boolean.TRUE);
        when(indexableTweets.isAnalyzed()).thenReturn(Boolean.TRUE);

        document = twitterResourceHandler.handleResource(indexContext, indexableTweets, document, tweet);
        assertEquals("The name of the user must be in the document : ", "nassereem1300", document.get("from-user"));
        assertEquals("The language of the user must be in the document : ", "ar", document.get("user-language"));
    }

    @Test
    @Ignore
    public void mergeCountryCityLanguageCoordinate() throws Exception {
        File countryCityFile = FileUtilities.findFileRecursively(new File("."), "country-city-language-coordinate.properties");
        File countryLanguageFile = FileUtilities.findFileRecursively(new File("."), "country-language.properties");
        logger.info(countryCityFile.getAbsolutePath());
        logger.info(countryLanguageFile.getAbsolutePath());
        List<String[]> countryCity = loadProperties(countryCityFile);
        List<String[]> countryLanguage = loadProperties(countryLanguageFile);
        for (final String[] country : countryCity) {
            for (final String[] language : countryLanguage) {
                if (country[0].equals(language[0])) {
                    Coordinate coordinate = getCoordinate(country[0]);
                    System.out.println(country[0] + "|" + country[1] + "|" + language[1] + "|" + coordinate.getLatitude() + "|" + coordinate.getLongitude());
                }
            }
        }
    }

    protected static HttpClient HTTP_CLIENT = new AutoRetryHttpClient();

    private Coordinate getCoordinate(final String searchString) throws Exception {
        Search search = new Search();
        search.setIndexName(IConstants.GEOSPATIAL);

        search.setSearchStrings(Arrays.asList(searchString));
        search.setSearchFields(Arrays.asList(IConstants.NAME));

        search.setFirstResult(0);
        search.setMaxResults(10);
        search.setFragment(Boolean.TRUE);

        String content = IConstants.GSON.toJson(search);

		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			@Override
			public String handleResponse(final HttpResponse response) {
				try {
					return FileUtilities.getContents(response.getEntity().getContent(), Long.MAX_VALUE).toString();
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
		HttpEntity httpEntity = new StringEntity(IConstants.GSON.toJson(search), APPLICATION_JSON);
		HttpPost postMethod = new HttpPost(getUrl(""));
		postMethod.addHeader(IConstants.CONTENT_TYPE, IConstants.APPLICATION_JSON);
		postMethod.setEntity(httpEntity);
		String response = HTTP_CLIENT.execute(postMethod, responseHandler);

        Search result = IConstants.GSON.fromJson(response, Search.class);
        Map<String, String> firstResult = result.getSearchResults().get(0);

        String latitude = firstResult.get(IConstants.LATITUDE);
        String longitude = firstResult.get(IConstants.LONGITUDE);
        return new Coordinate(Double.parseDouble(latitude), Double.parseDouble(longitude), firstResult.get(IConstants.NAME));
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    protected String getUrl(String path) throws MalformedURLException {
        StringBuilder builder = new StringBuilder();
        builder.append(IConstants.SEP);
        builder.append(IConstants.IKUBE);
        builder.append(IConstants.SEP);
        builder.append("service");
        builder.append(IConstants.SEP);
        builder.append(IConstants.SEARCH);
        builder.append(IConstants.SEP);
        builder.append("json");
        builder.append(path);
        return new URL("http", "ikube.be", 8080, builder.toString()).toString();
    }

    private List<String[]> loadProperties(final File file) {
        Reader reader = null;
        CSVReader csvReader = null;
        try {
            reader = new FileReader(file);
            csvReader = new CSVReader(reader, '|');
            return csvReader.readAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvReader);
        }
    }

}
