<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--
	This page is for searching the indexes using specific fields and will render 
	the indexables and search fields in the form dynamically for the indexes and indexables. At 
	the time of writing this page was not being used but should still be made available in the
	future. 
 -->

<c:set var="targetSearchUrl" value="/admin/search.html" />
<form name="ikubeSearchForm" id="ikubeSearchForm" action="<c:url value="${targetSearchUrl}"/>">
	<input name="targetSearchUrl" type="hidden" value="${targetSearchUrl}">
	<fieldset>
		<input type="text" name="searchStrings" id="search-text" 	value="<c:out value='${searchStrings}' />" />
		<input type="submit" id="search-submit" value="Go" />
	</fieldset>
</form>
<c:if test="${corrections != null}">
	Did you mean : 
	<a href="<c:url value="${targetSearchUrl}" />?targetSearchUrl=${targetSearchUrl}&searchStrings=${corrections}">${corrections}</a><br>
</c:if>

<table>
	<tr>
		<th class="td-content" colspan="2">Results</th>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			<table>
				<jsp:include page="/WEB-INF/jsp/include.jsp" flush="true" />
			</table>
		</td>
	</tr>
	
	<tr>
		<td class="bottom-content">Search per server and index</td>
	</tr>
</table>