package ikube.toolkit.data;

import ikube.ATest;
import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.XmlUtilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Element;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DataGeneratorMedicalTest extends ATest {

	private String	searchUrl	= "http://mapp.truvo.com/BE/yellow/search.ds?what=";

	public DataGeneratorMedicalTest() {
		super(DataGeneratorMedicalTest.class);
	}

	@Test
	public void generate() throws Exception {
		ApplicationContextManager.getApplicationContext();
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		File wordsFile = FileUtilities.findFileRecursively(new File("."), "english.txt");
		String wordsData = FileUtilities.getContents(wordsFile.toURI().toURL().openStream(), Short.MAX_VALUE).toString();
		StringTokenizer stringTokenizer = new StringTokenizer(wordsData);
		List<String> words = new ArrayList<String>();
		while (stringTokenizer.hasMoreTokens()) {
			words.add(stringTokenizer.nextToken());
		}
		Collections.shuffle(words);
		int index = 0;
		for (String word : words) {
			String newSearchUrl = searchUrl + word;
			logger.info("Search url : " + newSearchUrl);
			URL url = new URL(newSearchUrl);
			String contents = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toString();
			Element totalNumberOfResultsElement = XmlUtilities.getElement(
					XmlUtilities.getDocument(new ByteArrayInputStream(contents.getBytes()), IConstants.ENCODING).getRootElement(),
					"totalNumberOfResults");
			int totalResults = Integer.parseInt(totalNumberOfResultsElement.getText());
			DataGeneratorMedical dataGenerator = new DataGeneratorMedical(dataBase);
			dataGenerator.before();
			dataGenerator.setInputStream(new ByteArrayInputStream(contents.getBytes()));
			dataGenerator.generate();
			if (totalResults > 20) {
				int offset = 0;
				while (offset < totalResults) {
					String pagingSearchUrl = newSearchUrl + "&offset=" + offset;
					logger.info("Paging search url : " + pagingSearchUrl);
					offset += 20;
					url = new URL(pagingSearchUrl);
					dataGenerator.setInputStream(url.openStream());
					dataGenerator.generate();
				}
			}
			if (index >= 100) {
				break;
			}
		}
	}
}