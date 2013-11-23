package ikube.web.service;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.model.Analysis;
import ikube.toolkit.ObjectToolkit;

import java.util.Arrays;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 18.11.2012
 * @version 01.00
 */
public class ResourceTest extends BaseTest {

	private static String russian = "Россия   русский язык  ";
	private static String german = "Produktivität";
	private static String french = "Qu'est ce qui détermine la productivité, et comment est-il mesuré?";
	private static String somthingElseAlToGether = "Soleymān Khāţer";

	private Resource resource;

	@Before
	public void before() {
		resource = new SearcherJson();
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void buildResponse() {
		String[] result = { russian, german, french, somthingElseAlToGether };
		Response response = resource.buildJsonResponse(result);
		Object entity = response.getEntity();
		logger.info("Entity : " + entity);
		logger.info("Entity : " + Arrays.deepToString(result));
		assertTrue("Must have the wierd characters : ", entity.toString().contains(somthingElseAlToGether));
		assertTrue("Must have the wierd characters : ", entity.toString().contains("ä"));

		Analysis<String, String> analysis = ObjectToolkit.populateFields(new Analysis(), Boolean.TRUE, 100, "exception");
		response = resource.buildJsonResponse(analysis);
		logger.info("Response : " + response.getEntity());
	}

	@Test
	@Ignore
	public void unmarshall() throws Exception {
		HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
		ServletInputStream servletInputStream = Mockito.mock(ServletInputStream.class);

		Mockito.when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
		Mockito.when(servletInputStream).thenReturn(servletInputStream);
		// {"analyzer":"07911","input":{},"output":{},"duration":0.8558978222850855,"id":6140850535293793569,
		// "buildable":{"filterType":"89383","analyzerType":"31933","algorithmType":"30853","trainingData":"21166","analysisData":"78159","trainingFilePath":"37643","analysisFilePath":"53349","log":true,"compressed":true,"id":4705088335511122996}
		// }
		// resource.unmarshall(Analysis.class, request);
	}

}