package ikube.javascript;

import ikube.Base;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.WebClientFactory;

import java.io.File;
import java.net.URL;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@Ignore
public class JavaScriptTest extends Base {

	private Context javaScriptContext;
	private ScriptableObject javaScriptObject;

	@Before
	public void setup() throws Exception {
		javaScriptContext = ContextFactory.getGlobal().enterContext();
		javaScriptContext.setOptimizationLevel(-1);
		javaScriptContext.setLanguageVersion(Context.VERSION_1_8);
		javaScriptObject = javaScriptContext.initStandardObjects();

		// Create the print function
		String printFunction = "function print(message) { java.lang.System.out.println(message); }";
		javaScriptContext.evaluateString(javaScriptObject, printFunction, "print", 1, null);

		// Assumes we have env.rhino.js as a resource on the classpath.
		loadJavaScriptFiles("env.rhino", "ikube.js");
		loadJavaScriptUrls("http://code.jquery.com/jquery-1.8.2.js", "http://code.jquery.com/ui/1.9.1/jquery-ui.js",
				"http://www.google-analytics.com/ga.js", "http://maps.google.com/maps/api/js?sensor=false");

		// This will load the home page DOM.
		run("window.location='http://ikube.be/ikube/search.jsp'");

		// Whatever happens on document ready.
		run("track()");
	}

	private void loadJavaScriptUrls(final String... urls) throws Exception {
		for (final String urlString : urls) {
			URL url = new URL(urlString);
			String javaScript = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toString();
			javaScriptContext.evaluateString(javaScriptObject, javaScript, url.getFile(), 1, null);
		}
	}

	private void loadJavaScriptFiles(final String... files) {
		for (final String fileName : files) {
			File file = FileUtilities.findFileRecursively(new File("."), fileName);
			String javaScript = FileUtilities.getContents(file, Integer.MAX_VALUE).toString();
			javaScriptContext.evaluateString(javaScriptObject, javaScript, fileName, 1, null);
		}
	}

	@Test
	public void javaScript() throws Exception {
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
		HtmlPage page = webClient.getPage("http://ikube.be/ikube/search.jsp");
		HtmlButtonInput button = (HtmlButtonInput) page.getElementById("button");
		button.click();
	}

	private String run(String js) throws Exception {
		Object result = javaScriptContext.evaluateString(javaScriptObject, js, "run", 1, null);
		return Context.toString(result);
	}

}
