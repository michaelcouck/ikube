package ikube.index.handler.internet.crawler;

import ikube.model.Url;

/**
 * This class runs the JavaScript on the page. If there is a JavaScript exception thin the error will be collected and can be displayed in
 * the output.
 * 
 * @author Michael Couck
 * @since 25.09.10
 * @version 01.00
 */
public class ScriptHandler extends Handler<Url> {

	/**
	 * This method will execute the JavaScript in the page.
	 * 
	 * @See {@link IHandler#handle(Url)}
	 */
	public void handle(Url url) {
		// TODO - implement me. We could use Rhino for executing the JavaScript
		// or some other library for the execution, perhaps index the result too.
	}

}
