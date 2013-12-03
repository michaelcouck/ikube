package ikube.web.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.BaseTest;
import ikube.model.Analysis;
import ikube.model.Search;
import ikube.toolkit.ObjectToolkit;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
	public void unmarshall() throws Exception {
		Search search = ObjectToolkit.populateFields(new Search(), Boolean.TRUE, 10, "id", "exception");
		final String json = resource.gson.toJson(search);
		final ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(json.getBytes());

		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
		ServletInputStream servletInputStream = mock(ServletInputStream.class);

		when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
		when(servletInputStream.read(any(byte[].class))).thenAnswer(new Answer<Integer>() {
			@Override
			public Integer answer(final InvocationOnMock invocation) throws Throwable {
				byte[] bytes = (byte[]) invocation.getArguments()[0];
				return arrayInputStream.read(bytes);
			}
		});

		resource.unmarshall(Analysis.class, httpServletRequest);
	}

	@Test
	public void split() {
		String searchString = "hello, world | there you are";
		String[] result = resource.split(searchString);
		assertEquals("hello, world ", result[0]);
		assertEquals(" there you are", result[1]);
	}

}