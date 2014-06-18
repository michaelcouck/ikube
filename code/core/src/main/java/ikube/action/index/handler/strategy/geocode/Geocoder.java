package ikube.action.index.handler.strategy.geocode;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import ikube.IConstants;
import ikube.model.Coordinate;
import ikube.model.Search;
import ikube.security.WebServiceAuthentication;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

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

    private AutoRetryHttpClient httpClient;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized Coordinate getCoordinate(final String searchString) {
        if (disabled) {
            return null;
        }
        HttpPost postMethod = null;
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
			postMethod = new HttpPost(searchUrl);
			postMethod.addHeader(IConstants.CONTENT_TYPE, IConstants.APPLICATION_JSON);
			postMethod.setEntity(httpEntity);
			String response = httpClient.execute(postMethod, responseHandler);

            LOGGER.info("Result from web service : " + response);

            Search result = IConstants.GSON.fromJson(response, Search.class);
            ArrayList<HashMap<String, String>> results = result.getSearchResults();
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
        httpClient = new AutoRetryHttpClient();
        URL url;
        try {
            url = new URL(searchUrl);
            // new WebServiceAuthentication().authenticate(httpClient, url.getHost(), url.getPort(), userid, password);
        } catch (final MalformedURLException e) {
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