<%@ taglib prefix="ikube" uri="http://ikube" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">Geo search</span>
			&nbsp;<c:out value="${server.address}" />
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
</table>

<c:set var="targetSearchUrl" value="/admin/geosearch.html" />
<form name="geoSearchForm" id="geoSearchForm" action="<c:url value="${targetSearchUrl}"/>">
<input name="targetSearchUrl" type="hidden" value="${targetSearchUrl}">
<input name="indexName" type="hidden" value="${indexName}">
<table width="100%">
	<tr><th>Geo search ${indexName}</th></tr>
	<tr>
		<td>All fields:</td>
		<td><input id="search-text" type="text" name="searchStrings" value="<c:out value='${searchStrings}' />" /></td>
	</tr>
	<tr>
		<td>Latitude:</td>
		<td><input id="search-text" type="text" name="latitude" value="<c:out value='${latitude}' />" /></td>
	</tr>
	<tr>
		<td>Longitude:</td>
		<td><input id="search-text" type="text" name="longitude" value="<c:out value='${longitude}' />" /></td>
	</tr>
	<tr>
		<td>Distance:</td>
		<td><input id="search-text" type="text" name="distance" value="<c:out value='${distance}' />" /></td>
	</tr>
	
	<tr>
		<td><input type="submit" id="search-submit" value="Go" /></td>
	</tr>
	
	<tr>
		<td>
			<c:if test="${!empty corrections}">
				<br>
				Did you mean : 
				<a href="<c:url value="${targetSearchUrl}" />?indexName=${indexName}&targetSearchUrl=${targetSearchUrl}&searchStrings=${corrections}">${corrections}</a><br>
			</c:if>
		</td>
	</tr>
</table>
</form>
	
<table width="100%">
	<jsp:include page="/WEB-INF/jsp/include.jsp" flush="true" />
</table>

<table width="100%">
	<tr>
		<td class="td-content">
			<strong>geo search</strong>&nbsp;
			Search the index and sort the results starting from a co-ordinate, with a maximum distance. 
		</td>
	</tr>
</table>