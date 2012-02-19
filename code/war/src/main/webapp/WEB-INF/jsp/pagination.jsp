<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%@ taglib prefix="ikube" uri="http://ikube" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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
  					<c:choose>
  						<c:when test="${!empty latitude && !empty longitude && !empty distance}">
  							<!-- This is the geospatial search url -->
  							<a href="<c:url value="${searchUrl}" />?indexName=${
  								indexName}&targetSearchUrl=${
  								searchUrl}&searchStrings=${
  								searchStrings}&firstResult=${
  								index}&maxResults=${
  								maxResults}&latitude=${
  								latitude}&longitude=${
  								longitude}&distance=${
  								distance}">${index}</a>
  						</c:when>
  						<c:otherwise>
  		  					<!-- Normal url for a single search -->
  		  					<a href="<c:url value="${searchUrl}" />?indexName=${
  		  						indexName}&targetSearchUrl=${
  		  						searchUrl}&searchStrings=${
  		  						searchStrings}&firstResult=${
  		  						index}&maxResults=${
  		  						maxResults}">${index}</a>
  						</c:otherwise>
  					</c:choose>
  				</c:if>
			</c:forEach> 
		</td>
	</tr>