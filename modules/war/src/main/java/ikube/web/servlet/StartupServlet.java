package ikube.web.servlet;

import ikube.IConstants;
import ikube.toolkit.ApplicationContextManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class StartupServlet extends HttpServlet {

	@Override
	public void init() throws ServletException {
		ApplicationContextManager.getApplicationContext(new String[] { IConstants.SPRING_CONFIGURATION_FILE });
	}

}
