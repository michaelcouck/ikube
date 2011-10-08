package ikube.index.spatial;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.index.spatial.enrich.IEnrichment;
import ikube.index.spatial.geocode.IGeocoder;
import ikube.model.Indexable;
import mockit.Cascading;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
	private IGeocoder geocoder;
	private SpatialEnrichmentInterceptor interceptor;
	String address = "8a Avenue Road, Cape Town South Africa";
	private int invocations;

	public SpatialEnrichmentInterceptorTest() {
		super(SpatialEnrichmentInterceptorTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks();
		coordinate = mock(Coordinate.class);
		when(coordinate.getLat()).thenReturn(48.00058d);
		when(coordinate.getLon()).thenReturn(7.00011d);

		enrichment = mock(IEnrichment.class);
		when(enrichment.getCoordinate(any(Indexable.class))).thenReturn(null);
		when(enrichment.buildAddress(any(Indexable.class), any(StringBuilder.class))).thenReturn(new StringBuilder(address));
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				invocations++;
				logger.info("Invocation : " + invocations);
				return null;
			}
		}).when(enrichment).addSpatialLocationFields(any(Coordinate.class), any(Document.class));

		geocoder = mock(IGeocoder.class);
		when(geocoder.getCoordinate(anyString())).thenReturn(new Coordinate(-33.9693580, 18.4622110, address));

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
		Object[] arguments = new Object[] { document, indexableColumn };
		interceptor.enrich(arguments);
		assertTrue(true);

		when(indexableColumn.getContent()).thenReturn(address);
		interceptor.enrich(arguments);
		assertTrue("There must be at least one invocation on the enricher : ", invocations > 0);
	}

}
