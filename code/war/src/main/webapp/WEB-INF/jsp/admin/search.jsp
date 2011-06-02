<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<form name="searchForm" id="${param.indexName}" action="<c:url value="/admin/search.html"/>">
<input type="hidden" name="indexName" value="${param.indexName}">
<input type="hidden" name="fragment" value="true">
<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="2">
			<span class="top-content-header">Search</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	
	<c:set var="counter" scope="page" value="0"/>
	<c:if test="${counter % 2 == 0}" >
		<c:set var="counter" scope="page" value="${counter + 1}"/>
	</c:if>

	<tr>
		<th colspan="2">${param.indexName}</th>
	</tr>
	
	<c:forEach var="indexable" items="${requestScope.indexables}">
	<tr>
		<td>${indexable.key}</td>
		<td></td>
	</tr>
	<tr>
		<td></td>
		<td>
			<table>
				<c:forEach var="searchField" items="${indexable.value}">
				<tr>
					<td width="150">
						${searchField}
					</td>
					<td>
						<input type="text" name="${searchField}" value="${param[searchField]}" id="search-text"  />
					</td>
				</tr>
				</c:forEach>
			</table>
		</td>
	</tr>
	</c:forEach>
	
	<tr>
		<td colspan="2"><input type="submit" id="search-submit-plain" value="Go" /></td>
	</tr>
</table>
</form>

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
		<td class="td-content" colspan="2">
			<strong>configuration</strong>&nbsp;
			This is the search page that will search the index defined above. This will search this server 
			i.e. the server that this url is pointing to. To search other servers that are currently running 
			you need to point the browser at that server, typically something like 
			http://secondserver.com:yourport/ikube.
		</td>
	</tr>
		
	<tr>
		<td class="bottom-content">Search per server and index</td>
	</tr>
</table>