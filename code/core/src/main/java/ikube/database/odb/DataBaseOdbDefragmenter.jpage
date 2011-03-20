package ikube.database.odb;

import org.apache.log4j.Logger;

/**
 * The database becomes fragmented and the size is rather large so this class checks to see if the object size is above a certain level then
 * defragments the database. In many cases the size can go from 30 meg to 0.5 meg just from a defragment.
 * 
 * @author Michael Couck
 * @since 20.12.10
 * @version 01.00
 */
public class DataBaseOdbDefragmenter implements Runnable {

	private Logger logger;
	private transient final String dataBaseFile;
	private transient final DataBaseOdb dataBase;
	private transient long dataBaseSize = 0;
	private transient final long dataBaseSizeIncrement = 10000;
	private transient final long sleep = 10000000;

	public DataBaseOdbDefragmenter(final String dataBaseFile, final DataBaseOdb dataBase) {
		this.logger = Logger.getLogger(this.getClass());
		this.dataBaseFile = dataBaseFile;
		this.dataBase = dataBase;
	}

	@Override
	public void run() {
		String currentDataBaseFile = dataBaseFile;
		while (true) {
			try {
				Thread.sleep(sleep);
			} catch (Exception e) {
				logger.error("", e);
			}
			synchronized (this) {
				long totalObjects = dataBase.getTotalObjects();
				// If the size of the database is bigger than the database size + the
				// increment then de-fragment the database
				boolean shouldDefragment = (totalObjects > dataBaseSize + dataBaseSizeIncrement);
				// logger.info("Database size : " + dataBaseSize + ", " + shouldDefragment + ", " + Thread.currentThread().hashCode());
				if (shouldDefragment) {
					dataBaseSize += dataBaseSizeIncrement;
					currentDataBaseFile = dataBase.defragment(currentDataBaseFile);
				}
			}
		}
	}

}
