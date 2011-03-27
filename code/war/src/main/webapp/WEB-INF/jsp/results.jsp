<%@page import="ikube.toolkit.SerializationUtilities"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="ikube.toolkit.FileUtilities"%>
<%@page import="java.net.URL"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="search" uri="http://ikube/search" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">Results</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	
	<search:searchTag searchUrl="http://localhost:9000/ikube/SearchServlet">
	<tr>
		<td>
			Total : <c:out value='${sessionScope.total}' />,
			for '<c:out value='${param.searchStrings}' />',
			took <c:out value='${sessionScope.duration}' /> ms<br /><br />
		</td>
	</tr>
	<tr>
		<td>
			<!--
				This is the search tag. The tag will extract the parameters from the request and access the SearchServlet. In the above
				searchForm the parameters specified are name, fragment, searchField and searchString. These parameters will be passed to
				the SearchServlet. The result will be returned in a serialised list of maps.  This list will be de-serialised and added to the
				session where they are accessible to the page via standard JSTL tags and some expression language.

				The searchUrl attribute in the search tag is where the search application is deployed.
			-->
			<c:forEach var="map" items="${results}">
				Title : <a style="color : #8F8F8F;" href="<c:out value="${map['id']}" />"><c:out value="${map['title']}" /></a><br />
				Fragment : <c:out value="${map['fragment']}" escapeXml="false" /><br />
				Url : <c:out value="${map['id']}" /><br />
				Score : <c:out value="${map['score']}" /><br /><br />
			</c:forEach>
		</td>
	</tr>
	</search:searchTag>
	<tr>
		<td>
			<!--
				This is the paging tag. It will print the paging urls to the page where the next series in the results can be accessed via a link.
			 -->
			<search:pagerTag searchUrl="http://localhost:9000/ikube/results.html">
				<a href="<search:linkTag />" style="#1A1A1A"><search:pageTag /></a>
			</search:pagerTag>
		</td>
	</tr>
	
	<tr>
		<td class="bottom-content">Results brought to you by Ikube</td>
	</tr>
</table>