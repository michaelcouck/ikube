package ikube.action.index.handler.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.handler.enrich.geocode.Coordinate;
import ikube.action.index.handler.enrich.geocode.IGeocoder;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mockit.Cascading;
import mockit.Deencapsulation;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 20.01.2012
 * @version 01.00
 */
public class GeospatialEnrichmentStrategyTest extends AbstractTest {

	@Cascading
	private IGeocoder geocoder;
	@Cascading
	private Coordinate coordinate;
	/** Class under test. */
	private GeospatialEnrichmentStrategy geospatialEnrichmentStrategy;

	public GeospatialEnrichmentStrategyTest() {
		super(GeospatialEnrichmentStrategyTest.class);
	}

	@Before
	public void before() {
		geospatialEnrichmentStrategy = new GeospatialEnrichmentStrategy();
		Deencapsulation.setField(geospatialEnrichmentStrategy, "startTierParam", Integer.valueOf(10));
		Deencapsulation.setField(geospatialEnrichmentStrategy, "endTierParam", Integer.valueOf(20));
		geospatialEnrichmentStrategy.initialize();
		when(indexableTable.isAddress()).thenReturn(Boolean.TRUE);
		when(indexableColumn.isAddress()).thenReturn(Boolean.TRUE);
	}

	@Test
	public void aroundProcess() throws Exception {
		Document document = getDocument(RandomStringUtils.random(16), "Some string to index", Index.ANALYZED);
		Deencapsulation.setField(geospatialEnrichmentStrategy, geocoder);

		IndexableTable indexableTable = new IndexableTable();
		IndexableColumn latitudeColumn = new IndexableColumn();
		latitudeColumn.setFieldName(IConstants.LATITUDE);
		latitudeColumn.setContent("1");

		IndexableColumn longitudeColumn = new IndexableColumn();
		longitudeColumn.setFieldName(IConstants.LONGITUDE);
		longitudeColumn.setContent("1");

		List<Indexable<?>> indexableColumns = new ArrayList<Indexable<?>>(Arrays.asList(latitudeColumn, longitudeColumn));
		indexableTable.setChildren(indexableColumns);
		boolean result = geospatialEnrichmentStrategy.aroundProcess(indexContext, indexableTable, document, null);
		printDocument(document);
		assertTrue(result);
		assertTrue(document.get(IConstants.LAT) != null);
		assertTrue(document.get(IConstants.LNG) != null);
		assertTrue(document.get("_tier_15") != null);
	}

	@Test
	public void getCoordinate() throws Exception {
		Deencapsulation.setField(geospatialEnrichmentStrategy, geocoder);
		Coordinate coordinate = geospatialEnrichmentStrategy.getCoordinate(indexableTable);
		assertNotNull(coordinate);
	}

	@Test
	public void buildAddress() throws Exception {
		String expected = "9a Avanue Road, Cape Town, South Africa";
		IndexableTable indexableTable = new IndexableTable();
		IndexableColumn indexableColumn = new IndexableColumn();
		indexableTable.setAddress(Boolean.TRUE);
		indexableColumn.setAddress(Boolean.TRUE);
		indexableColumn.setContent("9a Avanue Road, Cape Town, South Africa");
		indexableTable.setChildren(new ArrayList<Indexable<?>>(Arrays.asList(indexableColumn)));
		StringBuilder actual = geospatialEnrichmentStrategy.buildAddress(indexableTable, new StringBuilder());

		logger.info("Address : " + actual);
		assertEquals("The combined address from the columns : ", expected, actual.toString());
	}

}