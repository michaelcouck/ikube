package ikube.web.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.BaseTest;
import ikube.analytics.IAnalyticsService;
import ikube.model.Analysis;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AnalyzerTest extends BaseTest {

	/** Class under test */
	private Analyzer analyzer;

	@Before
	@SuppressWarnings("unchecked")
	public void before() throws Exception {
		Analysis<?, ?> analysis = mock(Analysis.class);
		analyzer = mock(Analyzer.class);

		when(analyzer.analyze(any(HttpServletRequest.class), any(UriInfo.class))).thenCallRealMethod();
		when(analyzer.unmarshall(any(Class.class), any(HttpServletRequest.class))).thenReturn(analysis);

		IAnalyticsService analyticsService = Mockito.mock(IAnalyticsService.class);
		Deencapsulation.setField(analyzer, analyticsService);
	}

	@Test
	public void analyze() {
		analyzer.analyze((HttpServletRequest) null, (UriInfo) null);
		Mockito.verify(analyzer, Mockito.atLeastOnce()).buildJsonResponse(any());
	}

}