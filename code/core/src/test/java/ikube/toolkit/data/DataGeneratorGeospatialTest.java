package ikube.toolkit.data;

import ikube.ATest;
import ikube.IConstants;
import ikube.model.geospatial.GeoName;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import it.assist.jrecordbind.Unmarshaller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import net.sf.flatpack.DataSet;
import net.sf.flatpack.DefaultParserFactory;
import net.sf.flatpack.Parser;
import net.sf.flatpack.ParserFactory;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.StringUtils;

import co.uk.hjcs.canyon.session.Session;
import co.uk.hjcs.canyon.session.SessionFactory;

@Ignore
public class DataGeneratorGeospatialTest extends ATest {

	private String fileName = "allCountries.txt";
	private File mappingFile = FileUtilities.findFileRecursively(new File("."), "geoname.xml");

	private File dataFile = new File(fileName);

	public DataGeneratorGeospatialTest() {
		super(DataGeneratorGeospatialTest.class);
	}

	@Test
	public void generate1() throws Exception {
		ApplicationContextManager.getApplicationContext();
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_DB2);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(entityManager, "canyon.cfg.xml", "geoname1", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate2() throws Exception {
		ApplicationContextManager.getApplicationContext();
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_DB2);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(entityManager, "canyon.cfg.xml", "geoname2", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate3() throws Exception {
		ApplicationContextManager.getApplicationContext();
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_DB2);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(entityManager, "canyon.cfg.xml", "geoname3", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate4() throws Exception {
		ApplicationContextManager.getApplicationContext();
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_DB2);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(entityManager, "canyon.cfg.xml", "geoname4", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate5() throws Exception {
		ApplicationContextManager.getApplicationContext();
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_DB2);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(entityManager, "canyon.cfg.xml", "geoname5", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate6() throws Exception {
		ApplicationContextManager.getApplicationContext();
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_DB2);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(entityManager, "canyon.cfg.xml", "geoname6", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate7() throws Exception {
		ApplicationContextManager.getApplicationContext();
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_DB2);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(entityManager, "canyon.cfg.xml", "geoname7", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate8() throws Exception {
		ApplicationContextManager.getApplicationContext();
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_DB2);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(entityManager, "canyon.cfg.xml", "geoname8", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	@Ignore
	public void readFile() {
		File file = FileUtilities.findFileRecursively(new File("."), fileName);
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			for (int i = 0; i < 1000; i++) {
				String line = bufferedReader.readLine();
				line = line.length() > 200 ? line.substring(0, 199) : line;
				logger.info("Line : " + line);
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
	}

	@Test
	@Ignore
	public void jRecordBind() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "geoname.xsd");
		FileReader fileReader = new FileReader(file);
		// Marshaller< GeoName> marshaller = new Marshaller<GeoName>(fileReader);
		Unmarshaller<GeoName> unmarshaller = new Unmarshaller<GeoName>(fileReader);
		File dataFile = new File(fileName);
		FileReader dataFileReader = new FileReader(dataFile);
		Iterator<GeoName> geoNameIterator = unmarshaller.unmarshall(dataFileReader);
		for (int i = 0; i < 100; i++) {
			logger.info("Geo name : " + geoNameIterator.next());
		}
	}

	@Test
	@Ignore
	public void flatPack() throws Exception {
		ParserFactory parserFactory = DefaultParserFactory.getInstance();
		Parser parser = parserFactory.newDelimitedParser(new FileReader(mappingFile), new FileReader(dataFile), '\t', '"', true);
		DataSet ds = parser.parse();
		for (int i = 0; i < 100; i++) {
			if (ds.next()) {
				logger.info(ds.getString("geonameid"));
			}

		}
	}

	@Test
	@Ignore
	public void canyon() {
		Session session = SessionFactory.getSession("canyon.cfg.xml", "geoname");
		GeoName geoName = session.next(GeoName.class);
		logger.info(geoName);
	}

	@Test
	@Ignore
	public void convert() throws Exception {
		File inputFile = FileUtilities.findFileRecursively(new File("."), fileName);
		File outputFile = FileUtilities.getFile("./code/core/src/main/resources/allCountriesCsv.txt", Boolean.FALSE);
		FileReader fileReader = new FileReader(inputFile);
		FileWriter fileWriter = new FileWriter(outputFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			line = StringUtils.replace(line, "\t", ";");
			line = line + "\n";
			fileWriter.write(line);
		}
		fileWriter.close();
		fileReader.close();
	}

	@Test
	@Ignore
	public void split() throws Exception {
		String path = "./code/core/src/main/resources/";
		File inputFile = FileUtilities.findFileRecursively(new File("."), "allCountriesCsv.txt");
		int number = 1;
		File outputFile = FileUtilities.getFile(path + "allCountries" + number + "Csv.txt", Boolean.FALSE);
		FileReader fileReader = new FileReader(inputFile);
		FileWriter fileWriter = new FileWriter(outputFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = null;
		int counter = 0;
		while ((line = bufferedReader.readLine()) != null) {
			if (++counter >= 1000000) {
				counter = 0;
				fileWriter.close();
				outputFile = FileUtilities.getFile(path + "allCountries" + ++number + "Csv.txt", Boolean.FALSE);
				fileWriter = new FileWriter(outputFile);
			}
			line = StringUtils.replace(line, "\t", ";");
			line = line + "\n";
			fileWriter.write(line);
		}
		fileWriter.close();
		fileReader.close();
	}

}