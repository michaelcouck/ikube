<%@ page import="java.net.URL" %>
<%@ page import="ikube.IConstants" %>
<%@ page import="ikube.toolkit.SerializationUtilities" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.apache.commons.httpclient.HttpClient" %>
<%@ page import="org.apache.commons.httpclient.methods.GetMethod" %>
<%@ page import="org.apache.commons.httpclient.NameValuePair" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>

<%!
// This method will just create the name value pairs for the http client
NameValuePair[] getNameValuePairs(String[] names, String[] values) {
	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	for (int i = 0; i < names.length && i < values.length; i++) {
		NameValuePair nameValuePair = new NameValuePair(names[i], values[i]);
		nameValuePairs.add(nameValuePair);
	}
	return nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]);
}
%>

<%
	try {
		String path = "/ikube/service/search/multi/spatial/all?";
		String url = new URL("http", "localhost", 9080, path).toString(); 
		String[] names = { "indexName", "searchStrings", "fragment", "firstResult", "maxResults", "distance", "latitude", "longitude" };
		String[] values = { 
			"geospatial", 
			request.getParameter("searchStrings"), 
			"true", 
			request.getParameter("firstResult"), 
			request.getParameter("maxResults"), 
			request.getParameter("distance"), 
			request.getParameter("latitude"), 
			request.getParameter("longitude") };

		NameValuePair[] params = getNameValuePairs(names, values);
		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		
		HttpClient httpClient = new HttpClient();
		int result = httpClient.executeMethod(getMethod);
		String xml = getMethod.getResponseBodyAsString();
		
		ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(xml);
		session.setAttribute(IConstants.RESULTS, results);
	} catch (Exception e) {
		e.printStackTrace();
	}
%>

<c:set var="total" value="${total}" />
<c:set var="firstResult" value="${firstResult}" />
<c:set var="maxResults" value="${maxResults}" />
<c:set var="searchStrings" value="${searchStrings}" />
<c:set var="duration" value="${duration}" />
<c:set var="toResults" value="${total < firstResult + maxResults ? firstResult + (total % 10) : firstResult + maxResults}" />

<body>
	<table>
		<tr>
			<td>
				From : <c:out value='${firstResult + 1}' />,
				to : <c:out value='${toResults}' />,
				total : <c:out value='${total}' />,
				for '<c:out value='${searchStrings}' />',
				took <c:out value='${duration}' /> ms
				<c:if test="${!empty searchStrings && !empty total}">
				</c:if>
			</td>
		</tr>
		<tr>
			<td>
				<c:forEach var="result" items="${results}">
					<c:forEach var="entry" items="${result}">
						<c:out value="${entry.key}" /> : <c:out value="${entry.value}" escapeXml="false" />
						<c:if test="${entry.key == 'name' || entry.key == 'path' || entry.key == 'id'}">
							<img alt="Document type" src='" />' height="15px" width="15px" align="bottom" />
						</c:if>
						<br>
					</c:forEach>
					<br>
				</c:forEach>
			</td>
		</tr>
	</table>
</body>

</html>