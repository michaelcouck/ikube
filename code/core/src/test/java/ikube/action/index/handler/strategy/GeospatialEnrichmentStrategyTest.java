package ikube.action.index.handler.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.handler.strategy.geocode.IGeocoder;
import ikube.model.Coordinate;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
		Deencapsulation.setField(geospatialEnrichmentStrategy, "maxGeohashLevels", Integer.valueOf(10));
		geospatialEnrichmentStrategy.initialize();
		when(indexableTable.isAddress()).thenReturn(Boolean.TRUE);
		when(indexableColumn.isAddress()).thenReturn(Boolean.TRUE);
	}

	@After
	public void after() {
		// Mockit.tearDownMocks();
	}

	@Test
	public void aroundProcess() throws Exception {
		Document document = getDocument(RandomStringUtils.random(16), "Some string to index", IConstants.CONTENTS);
		Deencapsulation.setField(geospatialEnrichmentStrategy, "geocoder", geocoder);

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
		indexableTable.setChildren(new ArrayList<Indexable<?>>(Arrays.asList(indexableColumn)));
		StringBuilder actual = geospatialEnrichmentStrategy.buildAddress(indexableTable, new StringBuilder());

		logger.info("Address : " + actual);
		assertEquals("The combined address from the columns : ", expected, actual.toString());
	}

}