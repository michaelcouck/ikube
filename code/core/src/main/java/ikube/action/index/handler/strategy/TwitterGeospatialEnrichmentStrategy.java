package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.model.Coordinate;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableTweets;
import ikube.model.geospatial.GeoCity;
import ikube.model.geospatial.GeoCountry;
import ikube.search.ISearcherService;
import ikube.toolkit.HASH;
import ikube.toolkit.STRING;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import static ikube.action.index.IndexManager.addNumericField;
import static ikube.action.index.IndexManager.addStringField;

/**
 * TODO : Document me.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11-12-2013
 */
@Component
public final class TwitterGeospatialEnrichmentStrategy extends AGeospatialEnrichmentStrategy {

    @Value("${max-geohash-levels-twitter}")
    int maxGeohashLevels = IConstants.MAX_GEOHASH_LEVELS;

    private AtomicInteger userProfileCoordinates = new AtomicInteger();
    private AtomicInteger languageTimezoneCoordinates = new AtomicInteger();
    private AtomicInteger searchCoordinates = new AtomicInteger();

    @Autowired
    @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
    private ISearcherService searcherService;

    public TwitterGeospatialEnrichmentStrategy() {
        this(null);
    }

    public TwitterGeospatialEnrichmentStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean aroundProcess(final IndexContext indexContext, final Indexable indexable, final Document document,
                                 final Object resource) throws Exception {
        if (IndexableTweets.class.isAssignableFrom(indexable.getClass()) &&
                resource != null && Tweet.class.isAssignableFrom(resource.getClass())) {
            IndexableTweets indexableTweets = (IndexableTweets) indexable;
            TwitterProfile twitterProfile = ((Tweet) resource).getUser();
            // Get the location from the geo tag : "geo":{"coordinates":[-33.9769,18.5080],"type":"Point"}
            // This needs to be added to the Spring Twitter API, I asked Craig Wells on Twitter to add the field
            setCoordinate(indexableTweets, twitterProfile, document);
        }
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

    private void setCoordinate(final IndexableTweets indexableTweets, final TwitterProfile twitterProfile, final Document document) {
        // The user doesn't fill in the location properly but the site automatically
        // chooses a time zone for the ip address, adding this to the language and cross
        // referencing with the language seems to be quite accurate
        Coordinate tweetLocation = getLocationFromUserProfile(twitterProfile);

        // Get the location based on the user input, expensive!
        // Coordinate userLocationCoordinate = searchForLocationCoordinates(twitterProfile.getLocation(), IConstants.NAME);

        if (tweetLocation == null) {
            // This is a fall back, and could be any one of the countries that have that language
            // as a primary language for the time zone, we can't be sure, but it is better than nothing
            // at least we get the time zone, i.e. longitude correct
            tweetLocation = getLocationFromLanguageAndTimeZone(document, twitterProfile);
        }

        //noinspection StatementWithEmptyBody
        if (tweetLocation == null) {
            // Too expensive!!
            // tweetLocation = searchForLocationCoordinates(twitterProfile.getLocation(), IConstants.NAME);
        }

        if (tweetLocation != null) {
            addStringField(indexableTweets.getLocationField(), tweetLocation.getName(), indexableTweets, document);
            addNumericField(IConstants.LATITUDE, Double.toString(tweetLocation.getLatitude()), document, Boolean.TRUE, indexableTweets.getBoost());
            addNumericField(IConstants.LONGITUDE, Double.toString(tweetLocation.getLongitude()), document, Boolean.TRUE, indexableTweets.getBoost());
            addSpatialLocationFields(tweetLocation, document);

            int totalCoordinates = userProfileCoordinates.get() + languageTimezoneCoordinates.get() + searchCoordinates.get();
            if (totalCoordinates != 0 && totalCoordinates % 1000 == 0) {
                logger.info("User profile coordinates : " + userProfileCoordinates.get() +
                        ", language/timezone : " + languageTimezoneCoordinates.get() +
                        ", search : " + searchCoordinates.get());
            }
        }
    }

    /**
     * This method will get the location of the tweet from the time zone of the user profile. Typically this is
     * accurate as Twitter selects an appropriate time zone for the user based on the ip. This also contains the city, and
     * generally this is the best choice for the tweet. When the 'geo-tag' is added in Spring Social, then the 'real' co-ordinate
     * for the tweet will be available and this can check the tweet first.
     *
     * @param twitterProfile the profile of the user, cannot be null
     * @return the co-ordinate of the tweet based on the time zone of the user, or null if not time zone can be found
     */
    private Coordinate getLocationFromUserProfile(final TwitterProfile twitterProfile) {
        Coordinate timeZoneCoordinate = null;
        String timeZone = twitterProfile.getTimeZone();
        // Get the time zone location
        if (!StringUtils.isEmpty(timeZone)) {
            // This seems to be the most accurate
            String city = getCityFromTimeZone(timeZone);
            if (!StringUtils.isEmpty(city)) {
                Long hash = HASH.hash(city.toLowerCase());
                GeoCity geoCity = GEO_CITY.get(hash);
                if (geoCity != null) {
                    timeZoneCoordinate = geoCity.getCoordinate();
                    userProfileCoordinates.getAndIncrement();
                }
            }
        }
        return timeZoneCoordinate;
    }

    private String getCityFromTimeZone(final String timeZone) {
        String[] utcTimeZoneLocations = StringUtils.split(timeZone, '/');
        return utcTimeZoneLocations[utcTimeZoneLocations.length - 1].toLowerCase();
    }

    /**
     * This method matches that UTC time offset of the user(which should be correct) with the language of the
     * countries in the time zone. Of course the GMT+3 time zone has many countries that have the Arabic as a primary
     * language, so this is pretty useless except for the longitude.
     *
     * @param document       the document that will be added to the index, we get possibly the language of the tweet
     *                       from there
     * @param twitterProfile the twitter profile for the user, this can not be null
     * @return the co-ordinate of the time zone and language, but could be null, and only accurate to the longitude
     */
    private Coordinate getLocationFromLanguageAndTimeZone(final Document document, final TwitterProfile twitterProfile) {
        Coordinate coordinate = null;
        int utcOffsetSeconds = twitterProfile.getUtcOffset();
        String profileLanguage = twitterProfile.getLanguage();
        String tweetLanguage = document.get(IConstants.LANGUAGE);

        if (!StringUtils.isEmpty(profileLanguage)) {
            Locale profileLocale = new Locale(profileLanguage);
            profileLanguage = profileLocale.getDisplayLanguage(Locale.ENGLISH);
        }

        if (!StringUtils.isEmpty(tweetLanguage)) {
            Locale tweetLocale = new Locale(tweetLanguage);
            tweetLanguage = tweetLocale.getDisplayLanguage(Locale.ENGLISH);
        }

        // Try the language and the UTC offset combination
        String[] utcTimeZones = TimeZone.getAvailableIDs(utcOffsetSeconds * 1000);
        for (final String utcTimeZone : utcTimeZones) {
            String city = getCityFromTimeZone(utcTimeZone);
            // Find the country where this city is so we can find the language and match it against the user language
            Long hash = HASH.hash(city);
            GeoCity geoCity = GEO_CITY.get(hash);
            if (geoCity != null) {
                // Try to find the location based on the time zone and matched to the language to get the latitude
                GeoCountry geoCountry = geoCity.getParent();
                String timeZoneLanguage = geoCountry.getLanguage();
                boolean profileLanguageMatch = profileLanguage != null && timeZoneLanguage.contains(profileLanguage);
                boolean tweetLanguageMatch = tweetLanguage != null && timeZoneLanguage.contains(tweetLanguage);
                if (profileLanguageMatch || tweetLanguageMatch) {
                    logger.debug("Taking the country location from the time zone : {} ", geoCountry);
                    coordinate = geoCity.getCoordinate();
                    languageTimezoneCoordinates.incrementAndGet();
                    break;
                }
            }
        }

        return coordinate;
    }

    private Coordinate searchForLocationCoordinates(final String location, final String searchField) {
        // We need to clean the text for Lucene
        String searchString = STRING.stripToAlphaNumeric(location);
        String[] searchStrings = new String[]{searchString};
        String[] searchFields = new String[]{searchField};
        ArrayList<HashMap<String, String>> results = searcherService.search(IConstants.GEOSPATIAL_CSV, searchStrings, searchFields, Boolean.FALSE, 0, 3);

        if (results != null && results.size() > 1) {
            HashMap<String, String> timeZoneLocationResult = results.get(0);
            String latitude = timeZoneLocationResult.get(IConstants.LATITUDE);
            String longitude = timeZoneLocationResult.get(IConstants.LONGITUDE);
            searchCoordinates.getAndIncrement();
            return new Coordinate(Double.parseDouble(latitude), Double.parseDouble(longitude), location);
        }

        return null;
    }

}