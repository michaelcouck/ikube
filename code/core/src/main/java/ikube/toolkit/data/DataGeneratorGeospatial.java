package ikube.toolkit.data;

import ikube.database.IDataBase;
import ikube.model.geospatial.GeoName;
import co.uk.hjcs.canyon.session.Session;
import co.uk.hjcs.canyon.session.SessionFactory;

/**
 * This class takes the OpenGis data that is in the comma separated file, converts the lines to GeoName objects using the great flat file
 * mapping library from mCanyon and inserts the entities into the database. The result of the insert can then be indexed and searches for
 * addresses can happen against the index and the result will contain the latitude and longitude values for the address, basically the
 * geolocation data is inserted by this class.
 * 
 * @author Michael Couck
 * @since 26.04.2011
 * @version 01.00
 */
public class DataGeneratorGeospatial extends ADataGenerator {

	private String mappingFile;
	private String sessionName;
	private int offset;

	public DataGeneratorGeospatial(IDataBase dataBase, String mappingFile, String sessionName, int offset) {
		super(dataBase);
		this.mappingFile = mappingFile;
		this.sessionName = sessionName;
		this.offset = offset;
	}

	/**
	 * {@inheritDoc}
	 */
	public void generate() throws Exception {
		Session session = SessionFactory.getSession(mappingFile, sessionName);
		int counter = 0;
		int total = 0;
		int currentOffset = 0;
		while (session.hasNext(GeoName.class)) {
			try {
				GeoName geoName = null;
				try {
					geoName = session.next(GeoName.class);
				} catch (Exception e) {
					logger.error("Exception accessing the next bean : ", e);
					continue;
				}
				if (currentOffset < offset) {
					continue;
				}
				dataBase.persist(geoName);
				if (++counter >= 1000) {
					total += counter;
					counter = 0;
					logger.info("Total addresses : " + total + ", " + geoName);
				}
			} catch (Exception e) {
				logger.error("Exception persisting geoname : ", e);
			}
		}
		session.close();
	}

}