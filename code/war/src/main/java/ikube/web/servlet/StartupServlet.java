package ikube.web.servlet;

import ikube.toolkit.ApplicationContextManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class StartupServlet extends HttpServlet {

	@Override
	public void init() throws ServletException {
		ApplicationContextManager.getApplicationContext();
	}

}
