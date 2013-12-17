package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.database.IDataBase;
import ikube.model.Coordinate;
import ikube.model.IndexContext;
import ikube.model.IndexableTweets;
import ikube.model.geospatial.GeoCity;
import ikube.model.geospatial.GeoCountry;
import ikube.search.ISearcherService;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This class simply takes the specific data from Twitter and adds it to the index.
 * 
 * @author Michael Couck
 * @since 19.06.13
 * @version 01.00
 */
public class TwitterResourceHandler extends ResourceHandler<IndexableTweets> {

	private AtomicLong counter;

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private ISearcherService searcherService;

	public void init() {
		counter = new AtomicLong(0);
		File file = FileUtilities.findFileRecursively(new File(IConstants.IKUBE_DIRECTORY), "country-city-language-coordinate.properties");
		loadCountries(file);
	}

	private void loadCountries(final File file) {
		int removed = dataBase.remove(GeoCity.DELETE_ALL);
		logger.info("Removed cities : " + removed);
		removed = dataBase.remove(GeoCountry.DELETE_ALL);
		logger.info("Removed countries : " + removed);
		Reader reader = null;
		CSVReader csvReader = null;
		try {
			reader = new FileReader(file);
			csvReader = new CSVReader(reader, '|');
			List<String[]> data = csvReader.readAll();
			for (final String[] datum : data) {
				GeoCity geoCity = new GeoCity();
				GeoCountry geoCountry = new GeoCountry();

				// Setting this here affects OpenJpa for some reason! WTF!?
				// geoCity.setName(datum[1]);
				geoCity.setCoordinate(new Coordinate(Double.parseDouble(datum[3]), Double.parseDouble(datum[4])));
				geoCity.setParent(geoCountry);

				geoCountry.setName(datum[0]);
				geoCountry.setLanguage(datum[2]);
				geoCountry.setChildren(Arrays.asList(geoCity));

				dataBase.persist(geoCountry);
				geoCity.setName(datum[1]);
				dataBase.merge(geoCity);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(csvReader);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws Exception
	 */
	@Override
	public Document handleResource(final IndexContext<?> indexContext, final IndexableTweets indexableTweets, final Document document, final Object resource)
			throws Exception {
		Tweet tweet = (Tweet) resource;

		// This is the unique id of the resource to be able to delete it
		String tweetId = Long.toString(tweet.getId());
		String createdAtField = indexableTweets.getCreatedAtField();
		String fromUserField = indexableTweets.getFromUserField();
		String textField = indexableTweets.getTextField();

		// NOTE to self: To be able to delete using the index writer the identifier field must be non analyzed and non tokenized/vectored!
		// IndexManager.addStringField(IConstants.ID, tweetId, document, Store.YES, Index.NOT_ANALYZED, TermVector.NO);
		IndexManager.addNumericField(IConstants.ID, tweetId, document, Store.YES);
		IndexManager.addNumericField(createdAtField, Long.toString(tweet.getCreatedAt().getTime()), document, Store.YES);
		IndexManager.addStringField(fromUserField, tweet.getFromUser(), indexableTweets, document);
		IndexManager.addStringField(textField, indexableTweets.getContent().toString(), indexableTweets, document);

		handleProfile(indexableTweets, document, tweet);

		if (counter.getAndIncrement() % 10000 == 0) {
			logger.info("Document : " + document);
		}

		return super.handleResource(indexContext, indexableTweets, document, resource);
	}

	void handleProfile(final IndexableTweets indexableTweets, final Document document, final Tweet tweet) {
		String userNameField = indexableTweets.getUserNameField();
		String userScreenNameField = indexableTweets.getUserScreenNameField();
		String userLocationField = indexableTweets.getUserLocationField();
		String userTimeZoneField = indexableTweets.getUserTimeZoneField();
		String userUtcOffsetField = indexableTweets.getUserUtcOffsetField();
		String userLanguageField = indexableTweets.getUserLanguageField();

		TwitterProfile twitterProfile = tweet.getUser();

		if (twitterProfile != null) {
			String userName = twitterProfile.getName();
			String userScreenName = twitterProfile.getScreenName();
			String userLocation = twitterProfile.getLocation();
			String userTimeZone = twitterProfile.getTimeZone();
			int userUtcOffset = twitterProfile.getUtcOffset();
			String userLanguage = twitterProfile.getLanguage();

			IndexManager.addStringField(userNameField, userName, indexableTweets, document);
			IndexManager.addStringField(userScreenNameField, userScreenName, indexableTweets, document);
			IndexManager.addStringField(userLocationField, userLocation, indexableTweets, document);
			IndexManager.addStringField(userTimeZoneField, userTimeZone, indexableTweets, document);
			IndexManager.addNumericField(userUtcOffsetField, Integer.toString(userUtcOffset), document, Store.YES);
			IndexManager.addStringField(userLanguageField, userLanguage, indexableTweets, document);
		}
	}

}