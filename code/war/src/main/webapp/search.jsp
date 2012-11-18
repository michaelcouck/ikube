<%@ taglib prefix="ikube" uri="http://ikube" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--
	This page is for searching a specific index, and advanced searching using 
	individual fields in the index. 
 -->
<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">Search</span>
			&nbsp;<c:out value="${server.address}" />
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
</table>

<c:set var="targetSearchUrl" value="/admin/search.html" />
<form name="indexSearchForm" id="indexSearchForm" action="<c:url value="${targetSearchUrl}"/>">
<input name="targetSearchUrl" type="hidden" value="${targetSearchUrl}">
<input name="indexName" type="hidden" value="${indexName}">
<table width="100%">
	<tr><th>Multi search ${indexName}</th></tr>
	<tr>
		<td>
			Search all fields: 
			<input id="search-text" type="text" name="searchStrings" value="<c:out value='${searchStrings}' />" />
			<input type="submit" id="search-submit" value="Go" />
		</td>
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
	
<c:set var="targetSearchUrl" value="/admin/search.html" />
<form name="ikubeIndexSearchForm" id="ikubeIndexSearchForm" action="<c:url value="${targetSearchUrl}"/>">
<input name="targetSearchUrl" type="hidden" value="${targetSearchUrl}">
<input name="indexName" type="hidden" value="${indexName}">
<table width="100%">
	<tr><th colspan="2">Individual field(s) search ${indexName}</th></tr>
	<c:forEach var="indexFieldNameEntry" items="${indexFieldNamesAndValues}">
		<tr>
			<td>
				${indexFieldNameEntry.key}
			</td>
			<td style="padding-bottom: 0;">
				<input id="search-text" type="text" name="${indexFieldNameEntry.key}" value="${indexFieldNameEntry.value}" />
			</td>
		</tr>
	</c:forEach>
	<tr>
		<td colspan="2">
			<input type="submit" id="search-submit" value="Go" />
		</td>
	</tr>
	<tr>
		<td colspan="2">
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
			<strong>search & advanced search</strong>&nbsp;
			Search the index, and advanced search on individual fields with complex syntax. 
		</td>
	</tr>
</table>