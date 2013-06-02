package ikube.action.index.handler.database;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.AbstractTest;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.ApplicationContextManager;

import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 06.04.2013
 * @version 01.00
 */
public class QueryBuilderTest extends AbstractTest {

	private QueryBuilder queryBuilder;
	private IndexableTable geonameTable;

	@BeforeClass
	public static void beforeClass() {
		ApplicationContextManager.closeApplicationContext();
		ApplicationContextManager.getApplicationContextFilesystem("src/test/resources/spring/spring-geo.xml");
	}

	@AfterClass
	public static void afterClass() {
		ApplicationContextManager.closeApplicationContext();
	}

	@Before
	public void before() {
		queryBuilder = new QueryBuilder();
		geonameTable = ApplicationContextManager.getBean("geoname");
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
		assertEquals(
				"select geoname.id, geoname.name, geoname.geonameid, geoname.city, alternatename.id, alternatename.geonameid, action.id, action.id from geoname geoname, "
						+ "alternatename alternatename, action action where geoname.id > 0 and alternatename.id > 0 and action.id > 0 and action.indexname is not null and geoname.geonameid "
						+ "= alternatename.geonameid and geoname.id >= 0 and geoname.id < 1000", sql);

		// Now we remove all the child tables, and the predicate
		Iterator<Indexable<?>> childIterator = geonameTable.getChildren().iterator();
		while (childIterator.hasNext()) {
			Indexable<?> child = childIterator.next();
			if (IndexableTable.class.isAssignableFrom(child.getClass())) {
				childIterator.remove();
			}
		}
		geonameTable.setPredicate(null);
		sql = queryBuilder.buildQuery(geonameTable, 0l, 1000l);
		assertEquals(
				"select geoname.id, geoname.name, geoname.geonameid, geoname.city from geoname geoname where geoname.id >= 0 and geoname.id < 1000",
				sql);
	}

	@Test
	@Ignore
	public void buildQueryProd() {
		ApplicationContextManager.closeApplicationContext();
		ApplicationContextManager.getApplicationContextFilesystem("src/test/resources/spring/spring-geo-prod.xml");
		geonameTable = ApplicationContextManager.getBean("geoname");
		String sql = queryBuilder.buildQuery(geonameTable, 0l, 1000l);
		assertEquals(
				"select geoname.id, geoname.name, geoname.city, geoname.country, geoname.asciiname, geoname.alternatenames, geoname.latitude, geoname.longitude, "
						+ "geoname.featureclass, geoname.featurecode, geoname.countrycode, geoname.timezone, geoname.cc2, geoname.geonameid, geoname.admin1code, "
						+ "geoname.admin2code, geoname.admin3code, geoname.admin4code, geoname.modification, geoname.population, geoname.elevation, geoname.gtopo30, "
						+ "alternatename.id, alternatename.geonameid, alternatename.alternatename from geoname geoname, alternatename alternatename where geoname.id >= 0 and "
						+ "geoname.id < 1000", sql);
	}

}