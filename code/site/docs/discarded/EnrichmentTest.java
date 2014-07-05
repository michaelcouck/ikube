package ikube.action.index.handler.enrich;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.handler.enrich.geocode.Coordinate;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;

import java.lang.reflect.Field;
import java.util.Arrays;

import mockit.Cascading;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class EnrichmentTest extends AbstractTest {

	@Cascading
	private Document document;
	// @Cascading
	private Coordinate coordinate;
	private IEnrichment enrichment;
	private double minKm = 0;
	private double maxKm = 10;

	@Before
	public void before() {
		Mockit.setUpMocks();
		coordinate = mock(Coordinate.class);
		enrichment = new Enrichment();
	}

	@After
	public void after() {
		// Mockit.tearDownMocks();
	}

	@Test
	public void buildAddress() {
		// Indexable<?>, StringBuilder
		String address = enrichment.buildAddress(indexableColumn, new StringBuilder()).toString();
		logger.info("Address : " + address);
		assertEquals(indexableColumn.getContent(), address);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getCoordinate() {
		// Indexable<?>
		double latitude = 50.7930727874172;
		double longitude = 4.36242219751376;
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
	public void setMaxKm() throws Exception {
		// double
		enrichment.setMaxKm(maxKm);
		Field startTierField = ReflectionUtils.findField(enrichment.getClass(), "startTier");
		startTierField.setAccessible(Boolean.TRUE);
		Object startTier = ReflectionUtils.getField(startTierField, enrichment);

		Field endTierField = ReflectionUtils.findField(enrichment.getClass(), "endTier");
		endTierField.setAccessible(Boolean.TRUE);
		Object endTier = ReflectionUtils.getField(endTierField, enrichment);

		assertEquals("", Integer.valueOf(0), (Integer) startTier, 1);
		assertEquals("", Integer.valueOf(15), (Integer) endTier, 1);
	}

	@Test
	public void addCartesianTiers() {
		// Coordinate, Document
		int startTier = 15;
		int endTier = 15;
		enrichment.addCartesianTiers(coordinate, document, startTier, endTier);
		// TODO Mock the document, spy on it and verify that there are tiers in the document
	}

	@Test
	public void addSpatialLocationFields() {
		// Coordinate, Document
		enrichment.addSpatialLocationFields(coordinate, document);
		// TODO Mock the document, spy on it and verify that there are location fields in the document
	}

}