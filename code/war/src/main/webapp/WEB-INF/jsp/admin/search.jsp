<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="2">
			<span class="top-content-header">Search</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	
	<tr>
		<td class="td-content">Server</td>
		<td class="td-content">${param.address}</td>
	</tr>
	
	<tr>
		<td class="td-content">${param.indexName}</td>
		<td class="td-content">
			<form name="searchForm" id="${param.indexName}" action="<c:url value="/admin/search.html"/>">
				<input type="hidden" name="address" value="${param.address}">
				<input type="hidden" name="indexName" value="${param.indexName}">
				<input type="hidden" name="fragment" value="true">
				<table>
					<c:forEach var="searchField" items="${requestScope.searchFields}">
						<tr>
							<td>${searchField}</td>
							<td><input type="text" name="${searchField}" value="${param[searchField]}" id="search-text"  /></td>
						</tr>
					</c:forEach>
					<tr>
						<td colspan="2">
							<input type="submit" id="search-submit-plain" value="Go" />
						</td>
					</tr>
				</table>
			</form>
		</td>
	</tr>
	
	<tr>
		<td class="td-content" colspan="2">
			&nbsp;
		</td>
	</tr>
	
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
		<td class="td-content" colspan="2">
			<strong>configuration</strong>&nbsp;
			This is the search page for individual servers. On this page there is one server defined. For this server there 
			are several indexes defined. You can search the individual indexes on this specific server using the search box. 
			There may be several fields defined in the indexes which can be searched separately.
		</td>
	</tr>
		
	<tr>
		<td class="bottom-content">Search per server and index</td>
	</tr>
</table>