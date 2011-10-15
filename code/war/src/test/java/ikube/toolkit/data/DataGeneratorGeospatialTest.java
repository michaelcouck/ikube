package ikube.toolkit.data;

import ikube.ATest;
import ikube.database.IDataBase;
import ikube.model.geospatial.GeoName;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.StringUtils;

import co.uk.hjcs.canyon.session.Session;
import co.uk.hjcs.canyon.session.SessionFactory;

/**
 * This isn't a test, it is just for loading the database with the data from the GeoNames data files.
 * 
 * @author Michael Couck
 * @since 02.06.11
 * @version 01.00
 */
@Ignore
public class DataGeneratorGeospatialTest extends ATest {

	private String	fileName	= "allCountries.txt";

	public DataGeneratorGeospatialTest() {
		super(DataGeneratorGeospatialTest.class);
	}

	@Test
	public void generate1() throws Exception {
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(dataBase, "canyon.cfg.xml", "geoname1", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate2() throws Exception {
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(dataBase, "canyon.cfg.xml", "geoname2", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate3() throws Exception {
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(dataBase, "canyon.cfg.xml", "geoname3", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate4() throws Exception {
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(dataBase, "canyon.cfg.xml", "geoname4", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate5() throws Exception {
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(dataBase, "canyon.cfg.xml", "geoname5", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate6() throws Exception {
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(dataBase, "canyon.cfg.xml", "geoname6", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate7() throws Exception {
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(dataBase, "canyon.cfg.xml", "geoname7", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
	public void generate8() throws Exception {
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		DataGeneratorGeospatial dataGeneratorGeospatial = new DataGeneratorGeospatial(dataBase, "canyon.cfg.xml", "geoname8", 0);
		dataGeneratorGeospatial.before();
		dataGeneratorGeospatial.generate();
		dataGeneratorGeospatial.after();
	}

	@Test
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
		FileReader fileReader = null;
		FileWriter fileWriter = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(inputFile);
			fileWriter = new FileWriter(outputFile);
			bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				line = StringUtils.replace(line, "\t", ";");
				line = line + "\n";
				fileWriter.write(line);
			}
		} finally {
			FileUtilities.close(bufferedReader);
			FileUtilities.close(fileReader);
			FileUtilities.close(fileWriter);
		}
	}

	@Test
	@Ignore
	public void split() throws Exception {
		String path = "./code/core/src/main/resources/";
		File inputFile = FileUtilities.findFileRecursively(new File("."), "allCountriesCsv.txt");
		int number = 1;
		File outputFile = FileUtilities.getFile(path + "allCountries" + number + "Csv.txt", Boolean.FALSE);
		FileReader fileReader = null;
		FileWriter fileWriter = null;
		BufferedReader bufferedReader = null;

		try {
			fileReader = new FileReader(inputFile);
			fileWriter = new FileWriter(outputFile);
			bufferedReader = new BufferedReader(fileReader);
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
		} finally {
			FileUtilities.close(bufferedReader);
			FileUtilities.close(fileReader);
			FileUtilities.close(fileWriter);
		}
	}

}