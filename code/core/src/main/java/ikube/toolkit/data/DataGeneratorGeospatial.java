package ikube.toolkit.data;

import ikube.model.geospatial.GeoName;

import javax.persistence.EntityManager;

import co.uk.hjcs.canyon.session.Session;
import co.uk.hjcs.canyon.session.SessionFactory;

/**
 * @author Michael Couck
 * @since 26.04.2011
 * @version 01.00
 */
public class DataGeneratorGeospatial extends ADataGenerator {

	private String mappingFile;
	private String sessionName;

	public DataGeneratorGeospatial(EntityManager entityManager, String mappingFile, String sessionName) {
		super(entityManager);
		this.mappingFile = mappingFile;
		this.sessionName = sessionName;
	}

	/**
	 * {@inheritDoc}
	 */
	public void generate() throws Exception {
		Session session = SessionFactory.getSession(mappingFile, sessionName);
		int counter = 0;
		int total = 0;
		entityManager.getTransaction().begin();
		while (session.hasNext(GeoName.class)) {
			try {
				GeoName geoName = session.next(GeoName.class);
				entityManager.persist(geoName);
				if (++counter >= 1000) {
					total += counter;
					counter = 0;
					logger.info("Total addresses : " + total + ", " + geoName);
					entityManager.flush();
					entityManager.clear();
					entityManager.getTransaction().commit();
					entityManager.getTransaction().begin();
				}
			} catch (Exception e) {
				logger.error("Exception persisting geoname : ", e);
				// throw new RuntimeException(e);
			}
		}
		session.close();
	}

}