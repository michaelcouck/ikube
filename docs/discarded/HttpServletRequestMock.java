package ikube.web.tag.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

public class HttpServletRequestMock implements HttpServletRequest {

	private Map<String, String[]> parameters;
	private HttpSession session;

	public HttpServletRequestMock() {
		parameters = new HashMap<String, String[]>();
		session = new HttpSessionMock();
	}

	@Override
	public String getAuthType() {
		return null;
	}

	@Override
	public String getContextPath() {
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		return null;
	}

	@Override
	public long getDateHeader(String name) {
		return 0;
	}

	@Override
	public String getHeader(String name) {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration getHeaderNames() {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration getHeaders(String name) {
		return null;
	}

	@Override
	public int getIntHeader(String name) {
		return 0;
	}

	@Override
	public String getMethod() {
		return null;
	}

	@Override
	public String getPathInfo() {
		return null;
	}

	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public String getQueryString() {
		return null;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public String getRequestURI() {
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return null;
	}

	@Override
	public String getServletPath() {
		return null;
	}

	@Override
	public HttpSession getSession() {
		return session;
	}

	@Override
	public HttpSession getSession(boolean create) {
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
	}

	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	@Override
	public Object getAttribute(String name) {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames() {
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		return null;
	}

	@Override
	public int getContentLength() {
		return 0;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return null;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public int getLocalPort() {
		return 0;
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration getLocales() {
		return null;
	}

	@Override
	public String getParameter(String name) {
		String[] parameter = parameters.get(name);
		return parameter != null && parameter.length > 0 ? parameter[0] : null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map getParameterMap() {
		return parameters;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration getParameterNames() {
		return new EnumerationMock<String>(parameters.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		return null;
	}

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return null;
	}

	@Override
	public String getRealPath(String path) {
		return null;
	}

	@Override
	public String getRemoteAddr() {
		return null;
	}

	@Override
	public String getRemoteHost() {
		return null;
	}

	@Override
	public int getRemotePort() {
		return 0;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	@Override
	public String getScheme() {
		return null;
	}

	@Override
	public String getServerName() {
		return null;
	}

	@Override
	public int getServerPort() {
		return 0;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void removeAttribute(String name) {
	}

	@Override
	public void setAttribute(String name, Object o) {
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
	}

	public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {
		return false;
	}

	public Part getPart(String arg0) throws IOException, IllegalStateException, ServletException {
		return null;
	}

	public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
		return null;
	}

	public void login(String arg0, String arg1) throws ServletException {
	}

	public void logout() throws ServletException {
	}

	public AsyncContext getAsyncContext() {
		return null;
	}

	public DispatcherType getDispatcherType() {
		return null;
	}

	public ServletContext getServletContext() {
		return null;
	}

	public boolean isAsyncStarted() {
		return false;
	}

	public boolean isAsyncSupported() {
		return false;
	}

	public AsyncContext startAsync() {
		return null;
	}

	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) {
		return null;
	}

}
