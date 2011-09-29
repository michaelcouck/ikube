<%@ taglib prefix="ikube" uri="http://ikube" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--
	This page is for viewing the index and the fields in the index. 
 -->
<c:set var="renderedIndexables"  value="${ikube:add(null, null)}" scope="session" />
<c:set var="targetSearchUrl" value="/admin/index.html" />
<form name="ikubeSearchForm" id="ikubeSearchForm" action="<c:url value="${targetSearchUrl}"/>">
<input name="targetSearchUrl" type="hidden" value="${targetSearchUrl}">
<input name="indexName" type="hidden" value="${indexName}">
<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="2">
			<span class="top-content-header">Index</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr><th colspan="2">${indexContext.name}</th></tr>
	
	<tr>
		<td>
			<c:set var="children" value="${indexContext.indexables}" scope="session" />
			<jsp:include page="/WEB-INF/jsp/admin/indexable.jsp" />
		</td>
	</tr>
	<tr>
		<td>
			<input type="text" name="searchStrings" id="search-text" 	value="<c:out value='${searchStrings}' />" />
			<input type="submit" id="search-submit" value="Go" />
			<c:if test="${corrections != null}">
				<br>
				Did you mean : 
				<a href="<c:url value="${targetSearchUrl}" />?indexName=${indexName}&targetSearchUrl=${targetSearchUrl}&searchStrings=${corrections}">${corrections}</a><br>
			</c:if>
		</td>
	</tr>
</table>
</form>

<table>
	<jsp:include page="/WEB-INF/jsp/include.jsp" flush="true" />
</table>

<table>
	<tr>
		<td class="td-content" colspan="2">
			<strong>index</strong>&nbsp;
			This is the individual index properties and fields page.
		</td>
	</tr>
</table>