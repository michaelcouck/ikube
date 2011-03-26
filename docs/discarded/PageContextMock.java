package ikube.web.tag.mock;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

@SuppressWarnings("deprecation")
public class PageContextMock extends PageContext {

	private JspWriter jspWriter;
	private HttpServletRequest request;
	private Map<String, Object> attributes;

	public PageContextMock(JspWriter jspWriter, HttpServletRequest request) {
		this.jspWriter = jspWriter;
		this.request = request;
		this.attributes = new HashMap<String, Object>();
	}

	@Override
	public void forward(String relativeUrlPath) throws ServletException, IOException {
	}

	@Override
	public Exception getException() {
		return null;
	}

	@Override
	public Object getPage() {
		return null;
	}

	@Override
	public ServletRequest getRequest() {
		return request;
	}

	@Override
	public ServletResponse getResponse() {
		return null;
	}

	@Override
	public ServletConfig getServletConfig() {
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public HttpSession getSession() {
		return null;
	}

	@Override
	public void handlePageException(Exception e) throws ServletException, IOException {
	}

	@Override
	public void handlePageException(Throwable t) throws ServletException, IOException {
	}

	@Override
	public void include(String relativeUrlPath) throws ServletException, IOException {
	}

	@Override
	public void include(String relativeUrlPath, boolean flush) throws ServletException, IOException {
	}

	@Override
	public void initialize(Servlet servlet, ServletRequest request, ServletResponse response, String errorPageURL, boolean needsSession,
			int bufferSize, boolean autoFlush) throws IOException, IllegalStateException, IllegalArgumentException {
	}

	@Override
	public void release() {
	}

	@Override
	public Object findAttribute(String name) {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Object getAttribute(String name, int scope) {
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNamesInScope(int scope) {
		return null;
	}

	@Override
	public int getAttributesScope(String name) {
		return 0;
	}

	@Override
	public ELContext getELContext() {
		return null;
	}

	@Override
	public ExpressionEvaluator getExpressionEvaluator() {
		return null;
	}

	@Override
	public JspWriter getOut() {
		return jspWriter;
	}

	@Override
	public VariableResolver getVariableResolver() {
		return null;
	}

	@Override
	public void removeAttribute(String name) {
	}

	@Override
	public void removeAttribute(String name, int scope) {
	}

	@Override
	public void setAttribute(String name, Object value) {
		assert name != null;
		if (value != null) {
			attributes.put(name, value);
		} else {
			attributes.remove(name);
		}

	}

	@Override
	public void setAttribute(String name, Object value, int scope) {
	}

}
