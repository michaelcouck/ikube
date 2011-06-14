<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="search" uri="http://ikube/search" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

	<tr>
		<td>
			From : <c:out value='${firstResult + 1}' />,
			<c:set var="toResults" value="0" />			
			<c:choose>
				<c:when 
					test="${firstResult + maxResults > total}">
					<c:set var="toResults" 
						value="${firstResult + ((total - maxResults) % maxResults)}" />
				</c:when>
				<c:otherwise>
					<c:set var="toResults" value="${firstResult + maxResults}" />
				</c:otherwise>
			</c:choose>
			to : <c:out value='${toResults}' />,
			total : <c:out value='${total}' />,
			for '<c:out value='${searchStrings}' />',
			took <c:out value='${duration}' /> ms<br /><br />
		</td>
	</tr>
	<tr>
		<td>
			<!--
				
			-->
			<c:forEach var="map" items="${results}">
				Title : <a style="color : #8F8F8F;" href="<c:out value="${map['id']}" />"><c:out value="${map['title']}" /></a><br />
				Fragment : <c:out value="${map['fragment']}" escapeXml="false" /><br />
				Url : <c:out value="${map['id']}" /><br />
				Score : <c:out value="${map['score']}" />
				
				<!-- Put all the data in comments. -->
				<c:forEach var="entry" items="${map}">
					<!-- ${entry} -->
				</c:forEach>
				
				<br /><br />
			</c:forEach>
		</td>
	</tr>
	<tr>
		<td>
			<c:forEach var="i" begin="0" end="${total}" varStatus="counter">  
  				<c:if test="${i % 10 == 0}">
  					<a href="<c:url 
  						value="/results.html"  />?indexName=${indexName}&content=${searchStrings}&fragment=true&firstResult=${i}">${i}</a>
  				</c:if>
			</c:forEach> 
		</td>
	</tr>