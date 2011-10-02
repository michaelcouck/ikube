<%@ taglib prefix="ikube" uri="http://ikube" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--
	This page is for viewing the index and the fields in the index. 
 -->
<c:set var="renderedIndexables"  value="${ikube:add(null, null)}" scope="session" />
<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">Index</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr><th>${indexContext.name}</th></tr>
	<tr>
		<td>
			<c:set var="targetSearchUrl" value="/admin/index.html" />
			<form name="ikubeSearchForm" id="ikubeSearchForm" action="<c:url value="${targetSearchUrl}"/>">
			<input name="targetSearchUrl" type="hidden" value="${targetSearchUrl}">
			<input name="indexName" type="hidden" value="${indexName}">
			
			<table width="100%">
				<tr>
					<td>Search term: </td>
					<td><input type="text" name="searchStrings" id="searchStrings" 	value="<c:out value='${searchStrings}' />" /></td>
				</tr>
				<c:if test="${!empty geospatial && geospatial == 'true'}">
				<tr>
					<td>Latitude: </td>
					<td><input type="text" name="latitude" id="latitude" value="<c:out value='${latitude}' />" /></td>
				</tr>
				<tr>
					<td>Longitude: </td>
					<td><input type="text" name="longitude" id="longitude" value="<c:out value='${longitude}' />" /></td>
				</tr>
				<tr>
					<td>Distance: </td>
					<td><input type="text" name="distance" id="distance" value="<c:out value='${distance}' />" /></td>
				</tr>
				</c:if>
				<tr>
					<td colspan="2">
						<input type="submit" id="search-submit" value="Go" />
						<c:if test="${!empty corrections}">
							<br>
							Did you mean : 
							<a href="<c:url value="${targetSearchUrl}" />?indexName=${indexName}&targetSearchUrl=${targetSearchUrl}&searchStrings=${corrections}">${corrections}</a><br>
						</c:if>
					</td>
				</tr>
			</table>
			</form>
		</td>
	</tr>
</table>

<table width="100%">
	<jsp:include page="/WEB-INF/jsp/include.jsp" flush="true" />
</table>

<table width="100%">
	<tr>
		<td>
			<c:set var="children" value="${indexContext.indexables}" scope="session" />
			<jsp:include page="/WEB-INF/jsp/admin/indexable.jsp" />
		</td>
	</tr>
</table>

<table width="100%">
	<tr>
		<td class="td-content">
			<strong>index</strong>&nbsp;
			This is the individual index properties and fields page.
		</td>
	</tr>
</table>