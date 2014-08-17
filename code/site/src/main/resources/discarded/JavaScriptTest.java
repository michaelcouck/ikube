package discarded;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;
import ikube.web.toolkit.WebClientFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.net.URL;

@Ignore
public class JavaScriptTest extends AbstractTest {

	private Context context;
	private ScriptableObject scriptableObject;

	@Before
	public void setup() throws Exception {
		context = ContextFactory.getGlobal().enterContext();
		context.setOptimizationLevel(-1);
		context.setLanguageVersion(Context.VERSION_1_8);
		scriptableObject = context.initStandardObjects();

		// Create the print function
		String printFunction = "function print(message) { java.lang.System.out.println(message); }";
		context.evaluateString(scriptableObject, printFunction, "print", 1, null);

		// Assumes we have env.rhino.js as a resource on the classpath.
		loadJavaScriptFiles("env.rhino");
		loadJavaScriptUrls(//
				"http://ajax.googleapis.com/ajax/libs/angularjs/1.0.3/angular.min.js", //
				"http://code.jquery.com/jquery-1.8.2.js", //
				"http://code.jquery.com/ui/1.9.1/jquery-ui.js", //
				"http://www.google-analytics.com/ga.js", //
				"http://maps.google.com/maps/api/js?sensor=false");

		// This will load the home page DOM.
		// run("window.location='http://81.95.118.139/ikube/search.jsp'");
	}

	@Test
	public void javaScript() throws Exception {
		loadJavaScriptFiles("ikube.js");
		// Whatever happens on document ready.
		run("track()");
		String jsonParse = "JSON.parse = function parse(message) { java.lang.System.out.println('Parsing JSON'); }";
		Object result = context.evaluateString(scriptableObject, jsonParse, "parse", 1, null);
		logger.info("Result : " + result);

		context.evaluateString(scriptableObject, "jsonToArray();", "jsonToArray", 1, null);

		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
		logger.info("Script engine : " + scriptEngineManager + ", " + scriptEngine);
		scriptEngine.eval("print('Welcome to JavaScript world!');");
	}

	@Test
	public void jQuery() throws Exception {
		String statistics = run("$('#statistics')");
		logger.info("Statistics : " + statistics);
		String search = run("search('geospatial');");
		logger.info("Search : " + search);
		String results = run("$('#results').html();");
		logger.info("Results : " + results);
	}

	@Test
	public void htmlUnit() throws Exception {
		WebClient webClient = WebClientFactory.getWebClient(BrowserVersion.FIREFOX_3_6, true, false, false);
		HtmlPage page = webClient.getPage("http://81.95.118.139/ikube/search.jsp");
		HtmlButtonInput button = (HtmlButtonInput) page.getElementById("button");
		button.click();
	}

	private void loadJavaScriptUrls(final String... urls) throws Exception {
		for (final String urlString : urls) {
			URL url = new URL(urlString);
			String javaScript = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toString();
			context.evaluateString(scriptableObject, javaScript, url.getFile(), 1, null);
		}
	}

	private void loadJavaScriptFiles(final String... files) {
		for (final String fileName : files) {
			File file = FileUtilities.findFileRecursively(new File("."), fileName);
			String javaScript = FileUtilities.getContents(file, Integer.MAX_VALUE).toString();
			context.evaluateString(scriptableObject, javaScript, fileName, 1, null);
		}
	}

	private String run(String js) throws Exception {
		Object result = context.evaluateString(scriptableObject, js, "run", 1, null);
		return Context.toString(result);
	}

}
