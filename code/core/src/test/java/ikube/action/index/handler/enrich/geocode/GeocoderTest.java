package ikube.action.index.handler.enrich.geocode;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.handler.strategy.geocode.Geocoder;
import ikube.model.Coordinate;
import ikube.model.Search;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 06-03-2011
 */
@RunWith(MockitoJUnitRunner.class)
public class GeocoderTest extends AbstractTest {

    private Search search;

    @Spy
    @InjectMocks
    private Geocoder geocoder;
    @Mock
    private Client client;
    @Mock
    private WebResource webResource;
    @Mock
    private WebResource.Builder webResourceBuilder;
    @Mock
    private ClientResponse clientResponse;

    @Before
    public void before() {
        search = new Search();
        ArrayList<HashMap<String, String>> results = new ArrayList<>();

        HashMap<String, String> result = new HashMap<>();
        // Add the results that we are looking for
        result.put(IConstants.LATITUDE, "-33.9693580");
        result.put(IConstants.LONGITUDE, "18.4622110");
        results.add(result);
        // Add the statistics too
        result = new HashMap<>();
        results.add(result);

        search.setSearchResults(results);
    }

    @Test
    public void getCoordinate() throws Exception {
        when(client.resource(Matchers.anyString())).thenReturn(webResource);
        when(webResource.accept(any(MediaType.APPLICATION_JSON.getClass()))).thenReturn(webResourceBuilder);
        when(webResourceBuilder.post(any(Class.class), anyString())).thenReturn(clientResponse);
        when(clientResponse.getEntity(any(Class.class))).thenReturn(IConstants.GSON.toJson(search));

        geocoder.setSearchUrl("http://localhost:8080/ikube/service/search/json");
        geocoder.setSearchField("name");
        geocoder.setUserid("userid");
        geocoder.setPassword("password");

        Coordinate coordinate = geocoder.getCoordinate("9 avenue road, cape town, south africa");
        assertNotNull(coordinate);
        double lat = coordinate.getLatitude();
        double lon = coordinate.getLongitude();
        assertEquals(-33.9693580, lat, 1.0);
        assertEquals(18.4622110, lon, 1.0);
    }

}
