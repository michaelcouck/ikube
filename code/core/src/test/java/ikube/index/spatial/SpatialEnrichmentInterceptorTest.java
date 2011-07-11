package ikube.index.spatial;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.index.spatial.enrich.Enrichment;
import ikube.index.spatial.enrich.IEnrichment;
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
	@Cascading
	private Coordinate coordinate;
	private IEnrichment enrichment;
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

		enrichment = new Enrichment();
		enrichment.setMinKm(1);
		enrichment.setMaxKm(10);

		geocoder = new GoogleGeocoder();
		geocoder.setGeoCodeApi("http://maps.googleapis.com/maps/api/geocode/xml");

		interceptor = new SpatialEnrichmentInterceptor();
		interceptor.setEnrichment(enrichment);
		interceptor.setGeocoder(geocoder);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void enrich() {
		// Object[]
		Object[] arguments = new Object[] { document, INDEXABLE_COLUMN };
		interceptor.enrich(arguments);
		assertTrue(true);

		Object address = INDEXABLE_COLUMN.getContent();
		when(INDEXABLE_COLUMN.getContent()).thenReturn("8a Drongenhof, Ghent, België");
		interceptor.enrich(arguments);
		assertTrue(true);

		when(INDEXABLE_COLUMN.getContent()).thenReturn(address);
	}

}
