<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--
	This snippit is just for pagination so it can be included at various places
	in the search results' pages. 
 -->
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