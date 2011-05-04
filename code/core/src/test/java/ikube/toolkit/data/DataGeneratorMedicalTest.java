package ikube.toolkit.data;

import ikube.ATest;
import ikube.IConstants;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.net.URL;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DataGeneratorMedicalTest extends ATest {

	private String searchUrl = "http://mapp.truvo.com/BE/yellow/search.ds?what=";

	public DataGeneratorMedicalTest() {
		super(DataGeneratorMedicalTest.class);
	}

	@Test
	public void generate() throws Exception {
		ApplicationContextManager.getApplicationContext();
		EntityManager entityManager = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_ORACLE).createEntityManager();
		File wordsFile = FileUtilities.findFileRecursively(new File("."), "words.txt");
		String wordsData = FileUtilities.getContents(wordsFile.toURI().toURL().openStream(), Short.MAX_VALUE).toString();
		StringTokenizer stringTokenizer = new StringTokenizer(wordsData);
		for (int i = 0; i < 1000; i++) {
			String newSearchUrl = searchUrl + stringTokenizer.nextToken();
			logger.info("Search url : " + newSearchUrl);
			URL url = new URL(newSearchUrl);
			IDataGenerator dataGenerator = new DataGeneratorMedical(entityManager, url.openStream());
			dataGenerator.before();
			dataGenerator.generate();
		}
	}
}
