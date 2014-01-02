package ikube.action.index.handler.internet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Coordinate;
import ikube.model.IndexableTweets;
import ikube.model.Search;
import ikube.search.ISearcherService;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Deencapsulation;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.social.twitter.api.Tweet;

import au.com.bytecode.opencsv.CSVReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TwitterResourceHandlerTest extends AbstractTest {

	private IDataBase dataBase;
	private ISearcherService searcherService;
	private TwitterResourceHandler twitterResourceHandler;

	@Before
	@SuppressWarnings("unchecked")
	public void before() {
		dataBase = mock(IDataBase.class);
		searcherService = mock(ISearcherService.class);

		File file = FileUtilities.findFileRecursively(new File("."), "geospatial.results.xml");
		String xml = FileUtilities.getContents(file, Integer.MAX_VALUE).toString();
		ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(xml);
		when(searcherService.search(any(String.class), any(String[].class), any(String[].class), anyBoolean(), anyInt(), anyInt())).thenReturn(results);

		twitterResourceHandler = new TwitterResourceHandler();
		Deencapsulation.setField(twitterResourceHandler, "dataBase", dataBase);
		Deencapsulation.setField(twitterResourceHandler, "searcherService", searcherService);

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
		when(indexableTweets.getContent()).thenReturn("tweet-content");
		when(indexableTweets.getUserUtcOffsetField()).thenReturn("tweet-utc-offset");
		when(indexableTweets.isStored()).thenReturn(Boolean.TRUE);
		when(indexableTweets.isAnalyzed()).thenReturn(Boolean.TRUE);

		twitterResourceHandler.handleResource(indexContext, indexableTweets, document, tweet);
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

	protected static HttpClient HTTP_CLIENT = new HttpClient();

	private Coordinate getCoordinate(final String searchString) throws Exception {
		PostMethod postMethod = new PostMethod(getUrl(""));

		Search search = new Search();
		search.setIndexName(IConstants.GEOSPATIAL);

		search.setSearchStrings(Arrays.asList(searchString));
		search.setSearchFields(Arrays.asList(IConstants.NAME));

		search.setFirstResult(0);
		search.setMaxResults(10);
		search.setFragment(Boolean.TRUE);

		Gson gson = new Gson();
		String content = gson.toJson(search);
		StringRequestEntity stringRequestEntity = new StringRequestEntity(content, "application/json", IConstants.ENCODING);
		postMethod.setRequestEntity(stringRequestEntity);

		HTTP_CLIENT.executeMethod(postMethod);

		String json = FileUtilities.getContents(postMethod.getResponseBodyAsStream(), Integer.MAX_VALUE).toString();
		Search result = gson.fromJson(json, Search.class);
		Map<String, String> firstResult = result.getSearchResults().get(0);

		String latitude = firstResult.get(IConstants.LATITUDE);
		String longitude = firstResult.get(IConstants.LONGITUDE);
		return new Coordinate(Double.parseDouble(latitude), Double.parseDouble(longitude), firstResult.get(IConstants.NAME));
	}

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
