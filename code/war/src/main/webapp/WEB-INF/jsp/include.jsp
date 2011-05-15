<%@ page import="java.util.Map"%>
<%@ page import="java.util.List"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="search" uri="http://ikube/search" %>

	<%-- <search:searchTag searchUrl="${param.searchUrl}"> --%>
	<tr>
		<td>
			Total : <c:out value='${requestScope.total}' />,
			for '<c:out value='${requestScope.searchStrings}' />',
			took <c:out value='${requestScope.duration}' /> ms<br /><br />
		</td>
	</tr>
	<tr>
		<td>
			<!--
				This is the search tag. The tag will extract the parameters from the request and access the SearchServlet. In the above
				searchForm the parameters specified are name, fragment, searchField and searchString. These parameters will be passed to
				the SearchServlet. The result will be returned in a serialized list of maps.  This list will be de-serialized and added to the
				session where they are accessible to the page via standard JSTL tags and some expression language.

				The searchUrl attribute in the search tag is where the search application is deployed.
			-->
			<c:forEach var="map" items="${requestScope.results}">
				Title : <a style="color : #8F8F8F;" href="<c:out value="${map['id']}" />"><c:out value="${map['title']}" /></a><br />
				Fragment : <c:out value="${map['fragment']}" escapeXml="false" /><br />
				Url : <c:out value="${map['id']}" /><br />
				Score : <c:out value="${map['score']}" /><br /><br />
			</c:forEach>
		</td>
	</tr>
	<%-- </search:searchTag> --%>
	<tr>
		<td>
			<!--
				This is the paging tag. It will print the paging urls to the page where the next series in the results can be accessed via a link.
			 -->
			<search:pagerTag searchUrl="${param.searchUrl}">
				<a href="<search:linkTag />" style="#1A1A1A"><search:pageTag /></a>
			</search:pagerTag>
		</td>
	</tr>