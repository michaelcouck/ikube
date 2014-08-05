package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.handler.strategy.geocode.IGeocoder;
import ikube.model.Coordinate;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import mockit.Cascading;
import mockit.Deencapsulation;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @since 20.01.2012
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class GeospatialEnrichmentStrategyTest extends AbstractTest {

	@Cascading
	private IGeocoder geocoder;
	@Cascading
	private Coordinate coordinate;
	/** Class under test. */
	private GeospatialEnrichmentStrategy geospatialEnrichmentStrategy;

	@Before
	public void before() {
		geospatialEnrichmentStrategy = new GeospatialEnrichmentStrategy();
        Deencapsulation.setField(geospatialEnrichmentStrategy, "dataBase", dataBase);
        Deencapsulation.setField(geospatialEnrichmentStrategy, "maxGeohashLevels", 11);
		geospatialEnrichmentStrategy.initialize();
		when(indexableTable.isAddress()).thenReturn(Boolean.TRUE);
		when(indexableColumn.isAddress()).thenReturn(Boolean.TRUE);
	}

	@Test
	public void aroundProcess() throws Exception {
		Document document = getDocument(RandomStringUtils.random(16), "Some string to index", IConstants.CONTENTS);
		Deencapsulation.setField(geospatialEnrichmentStrategy, "geocoder", geocoder);
		// printDocument(document);

		IndexableTable indexableTable = new IndexableTable();
		IndexableColumn latitudeColumn = new IndexableColumn();
		latitudeColumn.setFieldName(IConstants.LATITUDE);
		latitudeColumn.setContent("19000");

		IndexableColumn longitudeColumn = new IndexableColumn();
		longitudeColumn.setFieldName(IConstants.LONGITUDE);
		longitudeColumn.setContent("19000");

		List<Indexable> indexableColumns = new ArrayList<Indexable>(Arrays.asList(latitudeColumn, longitudeColumn));
		indexableTable.setChildren(indexableColumns);
		boolean result = geospatialEnrichmentStrategy.aroundProcess(indexContext, indexableTable, document, null);
		printDocument(document);
		assertTrue(result);
		assertTrue(document.get(IConstants.POSITION_FIELD_NAME) != null);
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
		indexableColumn.setAddressContent("9a Avanue Road, Cape Town, South Africa");
		indexableTable.setChildren(new ArrayList<Indexable>(Arrays.asList(indexableColumn)));
		StringBuilder actual = geospatialEnrichmentStrategy.buildAddress(indexableTable, new StringBuilder());

		logger.info("Address : " + actual);
		assertEquals("The combined address from the columns : ", expected, actual.toString());
	}

}