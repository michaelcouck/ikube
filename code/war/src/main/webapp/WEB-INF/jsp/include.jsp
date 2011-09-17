<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--
	This page is for the results. To enable this page please have a look at the SearchController to know
	what must be included in the model for the page. Typically it is things like the first and maximum results, 
	also the total results for the search and of course the results them selves which are a list of maps. 
 -->
	<tr>
		<td>
			From : <c:out value='${firstResult + 1}' />,
			<c:set var="toResults" value="${total < firstResult + maxResults ? firstResult + (total % 10) : firstResult + maxResults}" />
			to : <c:out value='${toResults}' />,
			total : <c:out value='${total}' />,
			for '<c:out value='${searchStrings}' />',
			took <c:out value='${duration}' /> ms<br /><br>
		</td>
	</tr>
	<tr>
		<td>
			<jsp:include page="/WEB-INF/jsp/pagination.jsp" flush="true" /> 
		</td>
	</tr>
	<tr>
		<td>
			<c:forEach var="result" items="${results}">
				Title : <a style="color : #8F8F8F;" href="<c:out value="${result['title']}" />"><c:out value="${result['title']}" /></a><br />
				Fragment : <c:out value="${result['fragment']}" escapeXml="false" /><br />
				Url : <c:out value="${result['title']}" /><br />
				Score : <c:out value="${result['score']}" />
				<!-- Put all the data in comments. -->
				<c:forEach var="entry" items="${result}">
					<!-- ${entry} -->
				</c:forEach>
				<br>
			</c:forEach>
		</td>
	</tr>
	<tr>
		<td>
			<jsp:include page="/WEB-INF/jsp/pagination.jsp" flush="true" /> 
		</td>
	</tr>