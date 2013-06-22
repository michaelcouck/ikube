package ikube.web.service;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;

import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

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
	public void buildResponse() {
		String[] result = { russian, german, french, somthingElseAlToGether };
		Response response = resource.buildResponse(result);
		Object entity = response.getEntity();
		logger.info("Entity : " + entity);
		logger.info("Entity : " + Arrays.deepToString(result));
		assertTrue("Must have the wierd characters : ", entity.toString().contains(somthingElseAlToGether));
		assertTrue("Must contain the escaped sngle quote : ", entity.toString().contains("0027"));
	}

}
