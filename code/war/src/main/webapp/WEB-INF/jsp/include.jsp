<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="search" uri="http://ikube/search" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

	<tr>
		<td>
			From : <c:out value='${firstResult + 1}' />,
			<c:set var="toResults" value="${firstResult + maxResults}" />			
			<c:if test="${total < maxResults}">
				<c:set var="toResults" value="${firstResult + total}" />
			</c:if>
			to : <c:out value='${toResults}' />,
			total : <c:out value='${total}' />,
			for '<c:out value='${searchStrings}' />',
			took <c:out value='${duration}' /> ms<br /><br>
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
				<br><br>
			</c:forEach>
		</td>
	</tr>
	<tr>
		<td>
			<c:set var="maxResults" value="10" />
			<c:set var="total" value="${total > 150 ? 150 : total}"></c:set>
			<c:forEach var="index" begin="0" end="${total}" varStatus="counter"> 
  				<c:if test="${index % maxResults == 0}">
  					<c:set var="searchUrl" value="${targetSearchUrl != null ? targetSearchUrl : '/results.html'}" />
  					<a href="<c:url value="${searchUrl}"  />?targetSearchUrl=${searchUrl}&searchStrings=${searchStrings}&firstResult=${index}&maxResults=${maxResults}">${index}</a>
  				</c:if>
			</c:forEach> 
		</td>
	</tr>