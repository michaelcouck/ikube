package ikube.action.index.handler.database;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.AbstractTest;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.ApplicationContextManager;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 06.04.2013
 * @version 01.00
 */
public class QueryBuilderTest extends AbstractTest {

	private QueryBuilder queryBuilder;
	private IndexableTable geonameTable;

	public QueryBuilderTest() {
		super(QueryBuilderTest.class);
	}

	@Before
	public void before() {
		queryBuilder = new QueryBuilder();
		ApplicationContextManager.getApplicationContextFilesystem("src/test/resources/spring/spring-geo.xml");
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
		logger.info("Sql : " + sql);
		assertEquals(
				"select geoname.id, geoname.name, geoname.geonameid, geoname.city, alternatename.id, alternatename.geonameid, condition.id, condition.name from geoname "
						+ "geoname, alternatename alternatename, condition condition where geoname.id > 0 and alternatename.id > 0 and condition.id > 0 and condition.description "
						+ "is not null and geoname.geonameid = alternatename.geonameid and geoname.geonameid = condition.name and geoname.id >= 0 and geoname.id < 1000",
				sql);

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
		logger.info("Sql : " + sql);
		assertEquals(
				"select geoname.id, geoname.name, geoname.geonameid, geoname.city from geoname geoname where geoname.id >= 0 and geoname.id < 1000",
				sql);
	}

}