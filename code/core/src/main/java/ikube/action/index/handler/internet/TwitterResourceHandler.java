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
import ikube.toolkit.StringUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

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
		String locationField = indexableTweets.getLocationField();
		String userNameField = indexableTweets.getUserNameField();
		String userScreenNameField = indexableTweets.getUserScreenNameField();
		String userLocationField = indexableTweets.getUserLocationField();

		TwitterProfile twitterProfile = tweet.getUser();

		if (twitterProfile != null) {
			String userName = twitterProfile.getName();
			String userScreenName = twitterProfile.getScreenName();

			// Get the location from the geo tag : "geo":{"coordinates":[-33.9769,18.5080],"type":"Point"}
			// This needs to be added to the Spring Twitter API, I asked Craig Wells on Twitter to add the field

			IndexManager.addStringField(userNameField, userName, indexableTweets, document);
			IndexManager.addStringField(userScreenNameField, userScreenName, indexableTweets, document);
			IndexManager.addStringField(userLocationField, userName, indexableTweets, document);

			Coordinate userProfileLocation = getLocationFromUserProfile(twitterProfile);
			Coordinate tweetLocation = null;

			if (userProfileLocation != null) {
				// The user doesn't fill in the location properly but the site automatically
				// chooses a time zone for the ip address, adding this to the language and cross
				// referencing with the language seems to be quite accurate
				logger.debug("User profile location : {} ", userProfileLocation);
				tweetLocation = userProfileLocation;
			} else {
				Coordinate languageTimeZoneCoordinate = getLocationFromLanguageAndTimeZone(document, twitterProfile);
				if (languageTimeZoneCoordinate != null) {
					// This is a fall back, and could be any one of the countries that have that language
					// as a primary language for the time zone, we can't be sure, but it si better than nothing
					// at least we get the time zone, i.e. longitude correct
					logger.debug("Language time zone location : {} ", languageTimeZoneCoordinate);
					tweetLocation = languageTimeZoneCoordinate;
				}
			}
			if (tweetLocation != null) {
				indexableTweets.setCoordinate(tweetLocation);
				IndexManager.addStringField(locationField, tweetLocation.getName(), indexableTweets, document);
				IndexManager.addNumericField(IConstants.LATITUDE, Double.toString(tweetLocation.getLatitude()), document, Store.YES);
				IndexManager.addNumericField(IConstants.LONGITUDE, Double.toString(tweetLocation.getLongitude()), document, Store.YES);
			}
		}
	}

	/**
	 * This method matches that UTC time offset of the user(which should be correct) with the language of the countries in the time zone. Of course the GMT+3
	 * time zone has many countries that have the Arabic as a primary language, so this is pretty useless except for the longitude.
	 * 
	 * @param document the document that will be added to the index, we get possibly the language of the tweet from there
	 * @param twitterProfile the twitter profile for the user, this can not be null
	 * @return the co-ordinate of the time zone and language, but could be null, and only accurate to the longitude
	 */
	Coordinate getLocationFromLanguageAndTimeZone(final Document document, final TwitterProfile twitterProfile) {
		Coordinate coordinate = null;
		int utcOffsetSeconds = twitterProfile.getUtcOffset();
		String profileLanguage = twitterProfile.getLanguage();
		String tweetLanguage = document.get(IConstants.LANGUAGE);

		if (!StringUtils.isEmpty(profileLanguage)) {
			profileLanguage = new Locale(profileLanguage).getDisplayLanguage(Locale.ENGLISH);
		}

		if (!StringUtils.isEmpty(tweetLanguage)) {
			tweetLanguage = new Locale(tweetLanguage).getDisplayLanguage(Locale.ENGLISH);
		}

		// Try the language and the UTC offset combination
		String[] utcTimeZones = TimeZone.getAvailableIDs(utcOffsetSeconds * 1000);
		for (final String utcTimeZone : utcTimeZones) {
			String city = getCityFromTimeZone(utcTimeZone);
			// Find the country where this city is so we can find the language and match it against the user language
			GeoCity geoCity = dataBase.findCriteria(GeoCity.class, new String[] { IConstants.NAME }, new Object[] { city });
			if (geoCity != null) {
				// Try to find the location based on the time zone and matched to the language to get the latitude
				GeoCountry geoCountry = (GeoCountry) geoCity.getParent();
				String timeZoneLanguage = geoCountry.getLanguage();
				boolean profileLanguageMatch = profileLanguage != null ? timeZoneLanguage.contains(profileLanguage) : Boolean.FALSE;
				boolean tweetLanguageMatch = tweetLanguage != null ? timeZoneLanguage.contains(tweetLanguage) : Boolean.FALSE;
				if (profileLanguageMatch || tweetLanguageMatch) {
					logger.debug("Taking the country location from the time zone : {} ", geoCountry);
					coordinate = geoCity.getCoordinate();
					break;
				}
			}
		}

		return coordinate;
	}

	/**
	 * This method will get the location of the tweet from the time zone of the user profile. Typically this is accurate as the wite selects an appropriate time
	 * zone for the user based on the ip. This also contains the city, and generally this is the best choice for the tweet. When the 'geo-tag' is added in
	 * Spring Social, then the 'real' co-ordinate for the tweet will be available and this can check the tweet first.
	 * 
	 * @param twitterProfile the profile of the user, cannot be null
	 * @return the co-ordinate of the tweet based on the time zone of the user, or null if not time zone can be found
	 */
	Coordinate getLocationFromUserProfile(final TwitterProfile twitterProfile) {
		Coordinate timeZoneCoordinate = null;
		Coordinate userLocationCoordinate = null;
		String timeZone = twitterProfile.getTimeZone();
		String userLocation = twitterProfile.getLocation();

		// Get the time zone location
		if (!StringUtils.isEmpty(timeZone)) {
			// This seems to be the most accurate
			String city = getCityFromTimeZone(timeZone);

			GeoCity geoCity = dataBase.findCriteria(GeoCity.class, new String[] { IConstants.NAME }, new Object[] { city });
			if (geoCity != null) {
				timeZoneCoordinate = geoCity.getCoordinate();
			}
		}

		// Get the location based on the user input
		if (timeZoneCoordinate == null && !StringUtils.isEmpty(userLocation)) {
			userLocationCoordinate = findLocationCoordinates(userLocation, IConstants.NAME);
		}

		if (timeZoneCoordinate != null) {
			return timeZoneCoordinate;
		} else if (userLocationCoordinate != null) {
			return userLocationCoordinate;
		}

		return null;
	}

	String getCityFromTimeZone(final String timeZone) {
		String[] utcTimeZoneLocations = StringUtils.split(timeZone, '/');
		return utcTimeZoneLocations[utcTimeZoneLocations.length - 1];
	}

	Coordinate findLocationCoordinates(final String location, final String searchField) {
		// We need to clean the text for Lucene
		String searchString = StringUtilities.stripToAlphaNumeric(location);
		String[] searchStrings = new String[] { searchString };
		String[] searchFields = new String[] { searchField };
		ArrayList<HashMap<String, String>> results = searcherService.search(IConstants.GEOSPATIAL, searchStrings, searchFields, Boolean.FALSE, 0, 10);

		if (results != null && results.size() > 1) {
			HashMap<String, String> timeZoneLocationResult = results.get(0);
			String latitude = timeZoneLocationResult.get(IConstants.LATITUDE);
			String longitude = timeZoneLocationResult.get(IConstants.LONGITUDE);
			return new Coordinate(Double.parseDouble(latitude), Double.parseDouble(longitude), location);
		}

		return null;
	}

}