package ikube.action.index.handler.strategy.geocode;

import ikube.IConstants;
import ikube.model.Coordinate;
import ikube.model.Search;
import ikube.security.WebServiceAuthentication;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Couck
 * @version 01.00
 * @see IGeocoder
 * @since 06-03-2011
 */
public class Geocoder implements IGeocoder, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(Geocoder.class);

    private boolean disabled;
    private String userid;
    private String password;
    private String searchUrl;
    private String searchField;

    private HttpClient httpClient;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized Coordinate getCoordinate(final String searchString) {
        if (disabled) {
            return null;
        }
        PostMethod postMethod = null;
        try {
            // Trim the address for strange characters to get a better category
            String trimmedAddress = StringUtils.trim(searchString);
            Search search = new Search();
            search.setFirstResult(0);
            search.setFragment(Boolean.TRUE);
            search.setIndexName(IConstants.GEOSPATIAL);
            search.setMaxResults(10);
            search.setOccurrenceFields(Arrays.asList(IConstants.MUST));
            search.setSearchFields(Arrays.asList(searchField));
            search.setSearchStrings(Arrays.asList(searchString));
            search.setTypeFields(Arrays.asList(IConstants.STRING));

            StringRequestEntity requestEntity = new StringRequestEntity(IConstants.GSON.toJson(search), IConstants.APPLICATION_JSON, IConstants.ENCODING);
            postMethod = new PostMethod(searchUrl);
            postMethod.addRequestHeader(IConstants.CONTENT_TYPE, IConstants.APPLICATION_JSON);
            postMethod.setRequestEntity(requestEntity);

            int result = httpClient.executeMethod(postMethod);
            String json = postMethod.getResponseBodyAsString();
            LOGGER.info("Result from web service : " + result + ", " + json);

            Search response = IConstants.GSON.fromJson(json, Search.class);
            ArrayList<HashMap<String, String>> results = response.getSearchResults();
            if (results.size() > 1) {
                Map<String, String> firstResult = results.get(0);
                // We got a category, so we'll rely on Lucene to provide the best match for
                // the address according to the data from GeoNames
                String latitude = firstResult.get(IConstants.LATITUDE);
                String longitude = firstResult.get(IConstants.LONGITUDE);
                if (latitude != null && longitude != null) {
                    double lat = Double.parseDouble(latitude);
                    double lng = Double.parseDouble(longitude);
                    return new Coordinate(lat, lng, trimmedAddress);
                }
                LOGGER.info("Result from geoname search : " + firstResult);
            }
        } catch (final Exception e) {
            // We'll disable this geocoder for a while
            ThreadUtilities.submit(this.getClass().getSimpleName(), new Runnable() {
                public void run() {
                    try {
                        disabled = true;
                        ThreadUtilities.sleep(600000);
                    } finally {
                        disabled = false;
                        ThreadUtilities.destroy(this.getClass().getSimpleName());
                    }
                }
            });
            LOGGER.error("Address and geocoder : ", searchString, toString());
            LOGGER.error("Exception accessing the spatial search service : ", e);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        httpClient = new HttpClient();
        URL url;
        try {
            url = new URL(searchUrl);
            new WebServiceAuthentication().authenticate(httpClient, url.getHost(), Integer.toString(url.getPort()), userid, password);
        } catch (MalformedURLException e) {
            LOGGER.error(null, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSearchUrl(final String searchUrl) {
        this.searchUrl = searchUrl;
    }

    /**
     * This sets the search fields. At the time of writing the fields that were indexed in the GeoNames data
     * was 'name', 'city' and 'country'. The city and country fields are in fact the enriched data. Essentially
     * all three of these fields will be searched, in order and the best match for them aggregated will be used for the results.
     *
     * @param searchField the search field to search in the GeoSpatial index, typically this will be the name field
     *                    because this is an aggregation of the name of the feature in the GeoNames data and the enriched
     *                    fields for the city and the country
     */
    public void setSearchField(final String searchField) {
        this.searchField = searchField;
    }

    public void setUserid(final String userid) {
        this.userid = userid;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

}