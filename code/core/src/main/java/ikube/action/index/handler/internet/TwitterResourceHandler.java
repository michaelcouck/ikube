package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.IndexableTweets;
import ikube.model.geospatial.GeoZone;
import ikube.search.ISearcherService;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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

	private AtomicLong counter = new AtomicLong();
	private Properties countryLanguage;
	
	@Autowired
	private IDataBase dataBase;
	@Autowired
	private ISearcherService searcherService;

	private List<GeoZone> geoZones;
	private Map<Double, Collection<GeoZone>> gmtGeoZones;

	public void init() {
		counter = new AtomicLong(0);
		geoZones = new ArrayList<GeoZone>();
		gmtGeoZones = new HashMap<Double, Collection<GeoZone>>();
		countryLanguage = new Properties();
		InputStream inputStream = null;
		CSVReader csvReader = null;
		try {
			File ikubeFolder = new File(IConstants.IKUBE_DIRECTORY);
			File file = FileUtilities.findFileRecursively(ikubeFolder, "time-zones.csv");
			Reader reader = new FileReader(file);
			csvReader = new CSVReader(reader, ',');
			String[] headers = csvReader.readNext();
			String[] values = csvReader.readNext();
			BeanUtilsBean beanUtilsBean = BeanUtilsBean2.getInstance();
			do {
				GeoZone geoZone = new GeoZone();
				for (int i = 0; i < headers.length; i++) {
					beanUtilsBean.setProperty(geoZone, headers[i], values[i]);
				}
				geoZones.add(geoZone);
				values = csvReader.readNext();
			} while (values != null);
			Collections.sort(geoZones, new Comparator<GeoZone>() {
				@Override
				public int compare(final GeoZone o1, final GeoZone o2) {
					return Double.valueOf(o1.getGmt()).compareTo(Double.valueOf(o2.getGmt()));
				}
			});
			for (double zone = -12; zone < 12; zone++) {
				final double gmtZone = zone;
				@SuppressWarnings("unchecked")
				Collection<GeoZone> gmtGeoZone = CollectionUtils.select(geoZones, new Predicate() {
					@Override
					public boolean evaluate(final Object object) {
						GeoZone geoZone = (GeoZone) object;
						return gmtZone == geoZone.getGmt();
					}
				});
				gmtGeoZones.put(zone, gmtGeoZone);
			}
			File countryLanguageFile = FileUtilities.findFileRecursively(ikubeFolder, "country-language.properties");
			inputStream = new FileInputStream(countryLanguageFile);
			countryLanguage.load(inputStream);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			try {
				if (csvReader != null) {
					csvReader.close();
				}
			} catch (IOException e) {
				logger.error(null, e);
				throw new RuntimeException(e);
			}
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

		// handleProfile(indexableTweets, document, tweet);

		if (counter.getAndIncrement() % 10000 == 0) {
			logger.info("Document : " + document);
		}

		return super.handleResource(indexContext, indexableTweets, document, resource);
	}

	void handleProfile(final IndexableTweets indexableTweets, final Document document, final Tweet tweet) {

		String locationField = indexableTweets.getLocationField();
		String userNameField = indexableTweets.getUserNameField();
		String userScreenNameField = indexableTweets.getUserScreenNameField();
		String userLocationField = indexableTweets.getUserLocationField();

		TwitterProfile twitterProfile = tweet.getUser();

		if (twitterProfile != null) {

			String userName = twitterProfile.getName();
			String userLocation = twitterProfile.getLocation();
			String userScreenName = twitterProfile.getScreenName();
			String timeZone = twitterProfile.getTimeZone();
			int utcOffsetSeconds = twitterProfile.getUtcOffset();
			String userLanguage = twitterProfile.getLanguage();

			// Get the location from the geo tag : "geo":{"coordinates":[-33.9769,18.5080],"type":"Point"}
			// This needs to be added to the Spring Twitter API, I asked Craig Wells on Twitter to add the field

			String timeZoneLocation = null;
			String utcTimeZoneLocation = null;

			// Get the time zone location
			if (!StringUtils.isEmpty(timeZone)) {
				// This seems to be the most accurate
				timeZoneLocation = timeZone;
			}

			// Get the location based on the user input
			if (!StringUtils.isEmpty(userLocation)) {
				// Look for something in the database
				HashMap<String, String> result = findLocationCoordinates(userLocation, IConstants.NAME);
				if (result == null) {
					// Try the alternate names field
					result = findLocationCoordinates(userLocation, IConstants.ALTERNATE_NAMES);
				}
				if (result != null) {
					// We got a match to the user location that they filled in, could be anything of course, but
					// better than nothing and we'll take this as being gospel for the location of the user
					userLocation = result.get(IConstants.NAME);
				}
			}

			// Try the language and the UTC offset combination

			String[] utcTimeZones = TimeZone.getAvailableIDs(utcOffsetSeconds * 1000);
			@SuppressWarnings("unused")
			Collection<GeoZone> gmtGeoZone = gmtGeoZones.get(Double.valueOf(utcOffsetSeconds / 60d / 60d));
			if (StringUtils.isEmpty(userLanguage)) {
				// Get the document language if it exists
				userLanguage = document.get(IConstants.LANGUAGE);
			} else {
				// Get the platform default language name
				userLanguage = new Locale(userLanguage).getDisplayLanguage(Locale.ENGLISH);
			}
			if (StringUtils.isEmpty(userLanguage)) {
				// Take the first country on the time zone list
				utcTimeZoneLocation = utcTimeZones[0];
			} else {
				// Take the time zone that most closely matches either the
				// language of the user or the detected language of the tweet
				logger.info("Looking for language in time zones : " + userLanguage);
				for (final String utcTimeZone : utcTimeZones) {
					logger.info("Time zone : " + utcTimeZone);
					String[] utcTimeZoneLocations = StringUtils.split(utcTimeZone, '/');
					String city = utcTimeZoneLocations[utcTimeZoneLocations.length - 1];
					// Find the country where this city is so we can find the language and match it against the user language
					String country = null;
					if (!StringUtils.isEmpty(country)) {
						// Try to find the location based on the time zone and matched to the language to get the latitude
						String timeZoneLanguage = countryLanguage.getProperty(utcTimeZoneLocation);
						if (timeZoneLanguage != null && timeZoneLanguage.equals(userLanguage)) {
							logger.info("Taking the city location from the time zones : " + city + ", " + timeZoneLocation);
							timeZoneLocation = city;
							break;
						}
					} else {
						timeZoneLocation = city;
					}
				}
			}

			// Now find the co-ordinates of the user for the geospatial search
			HashMap<String, String> userLocationResult = findLocationCoordinates(userLocation, IConstants.NAME);
			if (userLocationResult != null) {
				userLocationResult.get(IConstants.LATITUDE);
				userLocationResult.get(IConstants.LONGITUDE);
			}

			HashMap<String, String> timeZoneLocationResult = findLocationCoordinates(timeZoneLocation, IConstants.NAME);
			if (timeZoneLocationResult != null) {
				timeZoneLocationResult.get(IConstants.LATITUDE);
				timeZoneLocationResult.get(IConstants.LONGITUDE);
			}

			IndexManager.addStringField(userNameField, userName, indexableTweets, document);
			IndexManager.addStringField(userScreenNameField, userScreenName, indexableTweets, document);
			IndexManager.addStringField(locationField, timeZoneLocation, indexableTweets, document);
			IndexManager.addStringField(userLocationField, userName, indexableTweets, document);
		}
	}

	private HashMap<String, String> findLocationCoordinates(final String location, final String searchField) {
		String[] searchStrings = new String[] { location };
		String[] searchFields = new String[] { searchField };
		ArrayList<HashMap<String, String>> results = searcherService.search(IConstants.GEOSPATIAL, searchStrings, searchFields, Boolean.FALSE, 0, 10);
		if (results.size() > 1) {
			return results.get(0);
		}
		return null;
	}

}
