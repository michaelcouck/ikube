package ikube.index.spatial;

import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.*;
import ikube.ATest;
import ikube.index.spatial.geocode.GoogleGeocoder;
import mockit.Cascading;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class SpatialEnrichmentInterceptorTest extends ATest {

	@Cascading
	private Document document;
	private Coordinate coordinate;
	private GoogleGeocoder geocoder;
	private SpatialEnrichmentInterceptor interceptor;

	public SpatialEnrichmentInterceptorTest() {
		super(SpatialEnrichmentInterceptorTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks();
		coordinate = mock(Coordinate.class);
		when(coordinate.getLat()).thenReturn(48.00058d);
		when(coordinate.getLon()).thenReturn(7.00011d);

		geocoder = new GoogleGeocoder();
		interceptor = new SpatialEnrichmentInterceptor();

		interceptor.setMinKm(1);
		interceptor.setMaxKm(10);
		interceptor.setGeocoder(geocoder);

		geocoder.setGeoCodeApi("http://maps.googleapis.com/maps/api/geocode/xml");
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void addCartesianTiers() {
		// Coordinate, Document
		int startTier = 15;
		int endTier = 15;
		interceptor.addCartesianTiers(coordinate, document, startTier, endTier);
	}

	@Test
	public void addSpatialLocationFields() {
		// Coordinate, Document
		interceptor.addSpatialLocationFields(coordinate, document);
	}

	@Test
	public void enrich() {
		// Object[]
		Object[] arguments = new Object[] { document, INDEXABLE };
		interceptor.enrich(arguments);
		assertTrue(true);

		Object address = INDEXABLE.getContent();
		when(INDEXABLE.getContent()).thenReturn("8a Drongenhof, Ghent, België");
		interceptor.enrich(arguments);
		assertTrue(true);

		when(INDEXABLE.getContent()).thenReturn(address);
	}

}
