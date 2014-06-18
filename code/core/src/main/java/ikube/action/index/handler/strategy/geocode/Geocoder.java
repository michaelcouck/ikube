package ikube.action.index.handler.strategy.geocode;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import ikube.IConstants;
import ikube.model.Coordinate;
import ikube.model.Search;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
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
public class Geocoder implements IGeocoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Geocoder.class);

    private boolean disabled;
    private String userid;
    private String password;
    private String searchUrl;
    private String searchField;

    private Client client = Client.create();

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized Coordinate getCoordinate(final String searchString) {
        if (disabled) {
            return null;
        }
        ClientResponse clientResponse = null;
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

            client.addFilter(new HTTPBasicAuthFilter(userid, password));
            WebResource webResource = client.resource(searchUrl);
            clientResponse = webResource.accept(MediaType.APPLICATION_JSON)
                    .post(ClientResponse.class, IConstants.GSON.toJson(search));
            String response = clientResponse.getEntity(String.class);

            LOGGER.debug("Result from web service : " + response);

            Search result = IConstants.GSON.fromJson(response, Search.class);
            ArrayList<HashMap<String, String>> results = result.getSearchResults();
            if (results.size() > 1) {
                Map<String, String> firstResult = results.get(0);
                // We got a category, so we'll rely on Lucene to provide the best match for
                // the address according to the data from GeoNames
                String latitude = firstResult.get(IConstants.LATITUDE);
                String longitude = firstResult.get(IConstants.LONGITUDE);
                if (StringUtils.isNotEmpty(latitude) && StringUtils.isNotEmpty(longitude)) {
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
            if (clientResponse != null) {
                clientResponse.close();
            }
        }
        return null;
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