package ikube.toolkit.data;

import ikube.ATest;
import ikube.model.geospatial.GeoName;
import ikube.toolkit.FileUtilities;
import it.assist.jrecordbind.Unmarshaller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;

import net.sf.flatpack.DataSet;
import net.sf.flatpack.DefaultParserFactory;
import net.sf.flatpack.Parser;
import net.sf.flatpack.ParserFactory;

import org.junit.Ignore;
import org.junit.Test;

import co.uk.hjcs.canyon.session.Session;
import co.uk.hjcs.canyon.session.SessionFactory;

import com.ibm.db2.jcc.DB2Driver;

// @Ignore
public class DataGeneratorGeospatialTest extends ATest {

	private String fileName = "allCountries.txt";
	private File mappingFile = FileUtilities.findFileRecursively(new File("."), "geoname.xml");
	private File dataFile = new File(fileName);

	public DataGeneratorGeospatialTest() {
		super(DataGeneratorGeospatialTest.class);
	}

	@Test
	@Ignore
	public void generate() throws Exception {
		Connection connection = null;
		try {
			String url = "jdbc:db2://localhost:50000/ikube";
			String user = "db2admin";
			String password = "db2admin";
			DriverManager.registerDriver(new DB2Driver());
			connection = null; // DriverManager.getConnection(url, user, password);
			// String fileName = "C:/Users/Administrator/Downloads/dump_postgres_openstreetmap_allcountries_v2/dump_localhost_street.sql";
			// String fileName = "C:/Users/Administrator/Downloads/changesets-110105.osm/changesets-110105.osm";
			DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(connection, mappingFile, dataFile);
			dataGeneratorGeospatial.before();
			dataGeneratorGeospatial.generate();
			dataGeneratorGeospatial.after();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					logger.error("Exceptino closing the connection : ", e);
				}
			}
		}
	}

	@Test
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
	public void canyon() {
		Session session = SessionFactory.getSession("canyon.cfg.xml", "geoname");
		GeoName geoName = session.next(GeoName.class);
		logger.info(geoName);
	}

	@Test
	public void convert() {
		File inputFile = FileUtilities.findFileRecursively(new File("."), fileName);
		File outputFile = FileUtilities.getFile("./allCountriesCsv.txt", Boolean.FALSE);
	}

}
