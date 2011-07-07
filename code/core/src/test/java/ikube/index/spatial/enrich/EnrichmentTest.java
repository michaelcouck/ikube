package ikube.index.spatial.enrich;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.IConstants;
import ikube.index.spatial.Coordinate;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;

import java.util.Arrays;

import mockit.Cascading;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EnrichmentTest extends ATest {

	@Cascading
	private Document document;
	private Coordinate coordinate;
	private IEnrichment enrichment;
	private double minKm = 0;
	private double maxKm = 10;

	public EnrichmentTest() {
		super(EnrichmentTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks();
		coordinate = mock(Coordinate.class);
		enrichment = new Enrichment();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void buildAddress() {
		// Indexable<?>, StringBuilder
		String address = enrichment.buildAddress(INDEXABLE, new StringBuilder()).toString();
		logger.info("Address : " + address);
		assertEquals(INDEXABLE.getContent(), address);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getCoordinate() {
		// Indexable<?>
		double latitude = new Double(50.7930727874172);
		double longitude = new Double(4.36242219751376);
		Indexable<?> tableIndexable = mock(Indexable.class);
		Indexable<?> columnIndexableLatitude = mock(IndexableColumn.class);
		Indexable<?> columnIndexableLongitude = mock(IndexableColumn.class);
		when(tableIndexable.getName()).thenReturn(IConstants.IKUBE);
		when(columnIndexableLatitude.getName()).thenReturn(IConstants.LATITUDE);
		when(columnIndexableLongitude.getName()).thenReturn(IConstants.LONGITUDE);
		when(((IndexableColumn) columnIndexableLatitude).getContent()).thenReturn(latitude);
		when(((IndexableColumn) columnIndexableLongitude).getContent()).thenReturn(longitude);

		when(tableIndexable.getChildren()).thenReturn(Arrays.asList(columnIndexableLatitude, columnIndexableLongitude));

		Coordinate coordinate = enrichment.getCoordinate(tableIndexable);
		logger.info("Coordinate : " + coordinate);
		assertEquals(latitude, coordinate.getLat(), 1);
		assertEquals(longitude, coordinate.getLon(), 1);
	}

	@Test
	public void setMinKm() {
		// double
		enrichment.setMinKm(minKm);
	}

	@Test
	public void setMaxKm() {
		// double
		enrichment.setMaxKm(maxKm);
	}

	@Test
	public void getMinKm() {
		int minKm = enrichment.getMinKm(1);
		logger.info("Min km : " + minKm);
		assertEquals(15, minKm, 1);
	}

	@Test
	public void getMaxKm() {
		int maxKm = enrichment.getMaxKm(10);
		logger.info("Max km : " + maxKm);
		assertEquals(15, maxKm, 1);
	}

	@Test
	public void addCartesianTiers() {
		// Coordinate, Document
		int startTier = 15;
		int endTier = 15;
		enrichment.addCartesianTiers(coordinate, document, startTier, endTier);
	}

	@Test
	public void addSpatialLocationFields() {
		// Coordinate, Document
		enrichment.addSpatialLocationFields(coordinate, document);
	}

}