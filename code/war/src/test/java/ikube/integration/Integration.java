package ikube.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableFileSystem;
import ikube.model.IndexableInternet;
import ikube.model.Server;
import ikube.model.faq.Faq;
import ikube.model.medical.Patient;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.data.DataGeneratorFour;
import ikube.toolkit.data.IDataGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * This test runs in its own Jenkins job. Typically it will be triggered by another job and will run along side other similar jobs
 * simulating a cluster. When the job finishes the index generated by the servers is tested.
 * 
 * @author Michael Couck
 * @since 20.12.10
 * @version 01.00
 */
public class Integration {

	static {
		Logging.configure();
	}

	protected Logger logger = Logger.getLogger(this.getClass());
	protected int iterations = 10;

	@Test
	public void main() throws Exception {
		if (!isServer()) {
			return;
		}
		Thread.sleep((long) (Math.random() * 10));
		ApplicationContextManager.getApplicationContext();

		// Insert some data into the medical database
		insertData(IConstants.PERSISTENCE_UNIT_H2, "nonXaDataSourceH2", iterations, new Class[] { Patient.class, Faq.class });
		// insertData(IConstants.PERSISTENCE_UNIT_DB2, "nonXaDataSourceDb2", iterations, new Class[] { Patient.class, Faq.class });
		// insertData(IConstants.PERSISTENCE_UNIT_ORACLE, "nonXaDataSourceOracle", iterations, new Class[] { Patient.class, Faq.class });

		waitToFinish();

		validateIndexes();
	}

	protected void insertData(String persistenceUnit, String dataSourceName, int iterations, Class<?>[] classes) throws Exception {
		logger.info("Inserting data into : " + persistenceUnit);
		ComboPooledDataSource dataSource = ApplicationContextManager.getBean(dataSourceName);
		Map<String, String> properties = getConnectionProperties(dataSource.getUser(), dataSource.getPassword(), dataSource.getJdbcUrl(),
				dataSource.getDriverClass());
		EntityManager entityManager = null;
		try {
			entityManager = Persistence.createEntityManagerFactory(persistenceUnit, properties).createEntityManager();
			IDataGenerator dataGenerator = new DataGeneratorFour(entityManager, iterations, classes);
			dataGenerator.before();
			dataGenerator.generate();
		} catch (Exception e) {
			logger.error("Exception inserting some data : ", e);
		}
	}

	private Map<String, String> getConnectionProperties(String user, String password, String url, String driver) {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("openjpa.ConnectionUserName", user);
		properties.put("openjpa.ConnectionPassword", password);
		properties.put("openjpa.ConnectionURL", url);
		properties.put("openjpa.ConnectionDriverName", driver);
		return properties;
	}

	protected void waitToFinish() {
		try {
			while (true) {
				Thread.sleep(600000);
				boolean anyWorking = ApplicationContextManager.getBean(IClusterManager.class).anyWorking();
				boolean isWorking = ApplicationContextManager.getBean(IClusterManager.class).getServer().getWorking();
				logger.info("Still working : " + anyWorking + ", " + isWorking);
				if (!anyWorking && !isWorking) {
					break;
				}
				logger.info("Members : " + Hazelcast.getCluster().getMembers().size());
				if (Hazelcast.getCluster().getMembers().size() == 1) {
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Exception waiting for servers to finish : ", e);
		}
	}

	@SuppressWarnings("unchecked")
	protected void validateIndexes() throws Exception {
		Server server = ApplicationContextManager.getBean(IClusterManager.class).getServer();
		List<String> webServiceUrls = server.getWebServiceUrls();
		for (String webServiceUrl : webServiceUrls) {
			if (!webServiceUrl.contains(ISearcherWebService.class.getSimpleName())) {
				continue;
			}
			ISearcherWebService searchRemote = ServiceLocator.getService(ISearcherWebService.class, webServiceUrl,
					ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
			Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
			for (String contextName : indexContexts.keySet()) {
				IndexContext indexContext = indexContexts.get(contextName);
				String indexName = indexContext.getIndexName();
				String[] searchStrings = new String[] { "quick", "brown", "fox", "jumped", "over", "the", "lazy", "dog" };
				String[] searchFields = getSearchFields(indexContext);
				String results = searchRemote.searchMulti(indexName, searchStrings, searchFields, Boolean.TRUE, 0, 11);
				logger.info("Results : " + results);
				assertNotNull(results);
				List<Map<String, String>> list = (List<Map<String, String>>) SerializationUtilities.deserialize(results);
				assertTrue(list.size() >= 1);
			}
		}
	}

	protected String[] getSearchFields(IndexContext indexContext) {
		List<String> searchFieldsList = new ArrayList<String>();
		List<Indexable<?>> indexables = indexContext.getIndexables();
		for (Indexable<?> indexable : indexables) {
			getSearchFields(indexable, searchFieldsList);
		}
		return searchFieldsList.toArray(new String[searchFieldsList.size()]);
	}

	protected void getSearchFields(Indexable<?> indexable, List<String> searchFieldsList) {
		String fieldName = null;
		if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
			fieldName = ((IndexableColumn) indexable).getFieldName();
		} else if (IndexableInternet.class.isAssignableFrom(indexable.getClass())) {
			fieldName = ((IndexableInternet) indexable).getContentFieldName();
		} else if (IndexableFileSystem.class.isAssignableFrom(indexable.getClass())) {
			fieldName = ((IndexableFileSystem) indexable).getContentFieldName();
		}
		if (fieldName == null) {
			fieldName = "content";
		}
		searchFieldsList.add(fieldName);
		List<Indexable<?>> children = indexable.getChildren();
		if (children != null) {
			for (Indexable<?> child : children) {
				getSearchFields(child, searchFieldsList);
			}
		}
	}

	protected boolean isServer() {
		Properties properties = System.getProperties();
		String osName = System.getProperty("os.name");
		logger.info("Operating system : " + osName + ", server : " + osName.toLowerCase().contains("server") + ", 64 bit : "
				+ properties.getProperty("os.arch").contains("64"));
		if (osName.toLowerCase().contains("server") && properties.getProperty("os.arch").contains("64")) {
			return Boolean.FALSE;
		}
		return Boolean.FALSE;
	}

}