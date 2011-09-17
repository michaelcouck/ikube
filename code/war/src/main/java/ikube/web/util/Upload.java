package ikube.web.util;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Upload {

	private static final String	GOOGLE_CODE_UPLOAD_PAGE	= "http://code.google.com/p/ikube/downloads/entry";

	public static void main(String[] args) throws Exception {
		WebClient webClient = WebClientFactory.getWebClient();
		HtmlPage page = webClient.getPage(GOOGLE_CODE_UPLOAD_PAGE);
		page.getFormByName("");
	}

}
