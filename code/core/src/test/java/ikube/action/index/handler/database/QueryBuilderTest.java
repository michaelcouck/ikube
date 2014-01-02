package ikube.action.index.handler.database;

import ikube.AbstractTest;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 06.04.2013
 */
public class QueryBuilderTest extends AbstractTest {

	private QueryBuilder queryBuilder;
	private IndexableTable geonameTable;

	@Before
	public void before() {
		ApplicationContextManager.closeApplicationContext();
		File file = FileUtilities.findFileRecursively(new File("."), "spring-geo-prod.xml");
		String filePath = FileUtilities.cleanFilePath(file.getAbsolutePath());
		// "src/test/resources/spring/spring-geo-prod.xml"
		ApplicationContextManager.getApplicationContextFilesystem("file:" + filePath);
		queryBuilder = new QueryBuilder();
		geonameTable = ApplicationContextManager.getBean("geoname");
	}

	@After
	public void after() {
		ApplicationContextManager.closeApplicationContext();
	}

	@Test
	public void getIdColumn() throws Exception {
		IndexableColumn idColumn = QueryBuilder.getIdColumn(geonameTable.getChildren());
		assertNotNull(idColumn);
		assertEquals("id", idColumn.getName());
	}

	@Test
	public void buildQuery() {
		String sql = queryBuilder.buildQuery(geonameTable, 0l, 1000l);
		logger.info(sql);
		assertTrue(sql
			.contains("select    geoname.id ,    geoname.name ,    geoname.city ,    geoname.country ,    " +
				"geoname.asciiname ,    geoname.alternatenames ,    "
				+ "geoname.latitude ,    geoname.longitude ,    geoname.featureclass ,    geoname.featurecode ,    " +
				"geoname.countrycode ,    geoname.timezone ,    "
				+ "geoname.cc2 ,    geoname.geonameid ,    geoname.admin1code ,    geoname.admin2code ,    " +
				"geoname.admin3code ,    geoname.admin4code ,    "
				+ "geoname.modification ,    geoname.population ,    geoname.elevation ,    geoname.gtopo30 ,    " +
				"alternatename.id ,    alternatename.geonameid ,    "
				+ "alternatename.alternatename from    geoname ,    alternatename where    geoname.id >= 0.0 and    " +
				"geoname.id < 1000.0"));
	}

	@Test
	public void buildNextIdQuery() {
		String nextIdQuery = QueryBuilder.buildNextIdQuery(geonameTable, 1000l);
		logger.info("Is query : " + nextIdQuery);

		assertEquals("select id from geoname where id >= 1000", nextIdQuery);

		nextIdQuery = QueryBuilder.buildNextIdQuery(geonameTable, 10000l);
		logger.info("Is query : " + nextIdQuery);
		assertEquals("select id from geoname where id >= 10000", nextIdQuery);
	}

	@Test
	public void buildQueryProd() {
		geonameTable = ApplicationContextManager.getBean("geoname");
		String sql = queryBuilder.buildQuery(geonameTable, 0l, 1000l);
		logger.info(sql);
		assertEquals(
			"select    geoname.id ,    geoname.name ,    geoname.city ,    geoname.country ,    geoname.asciiname ,   " +
				" geoname.alternatenames ,    geoname.latitude ,    "
				+ "geoname.longitude ,    geoname.featureclass ,    geoname.featurecode ,    geoname.countrycode ,    " +
				"geoname.timezone ,    geoname.cc2 ,    geoname.geonameid ,    "
				+ "geoname.admin1code ,    geoname.admin2code ,    geoname.admin3code ,    geoname.admin4code ,    " +
				"geoname.modification ,    geoname.population ,    geoname.elevation ,    "
				+ "geoname.gtopo30 ,    alternatename.id ,    alternatename.geonameid ,    " +
				"alternatename.alternatename from    geoname ,    alternatename where    geoname.id >= 0.0 and    "
				+ "geoname.id < 1000.0 ", sql);

		IndexableTable jsonCacheTable = ApplicationContextManager.getBean("json_cache");
		String tweetSql = queryBuilder.buildQuery(jsonCacheTable, 0l, 1000l);
		logger.info(tweetSql);

		IndexableTable indexContextTable = ApplicationContextManager.getBean("indexContextTable");
		String indexContextSql = queryBuilder.buildQuery(indexContextTable, 0l, 1000l);
		logger.info(indexContextSql);
	}

}