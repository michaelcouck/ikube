<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="9" valign="middle">
			<span class="top-content-header">Indexes</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>	
	<tr>
		<th class="td-content" colspan="2">Index</th>
		<th class="td-content">Docs</th>
		<th class="td-content">Size</th>
		<th class="td-content" colspan="2">Open</th>
		<th class="td-content" nowrap="nowrap">Max age</th>
		<th class="td-content">Timestamp</th>
		<th class="td-content" nowrap="nowrap">Index path</th>
	</tr>
	
	<c:forEach var="indexContext" items="${requestScope.indexContexts}">
	<tr>
		<td width="1">
			<img alt="Search index ${indexContext.name}" 
				src="<c:url value="/images/icons/search.gif" />" 
				title="Search index ${indexContext.name}">
		</td>
		<td class="td-content">
			<a href="<c:url value="/admin/index.html" />?indexName=${indexContext.name}" 
				style="font-style: italic;" 
				title="Search index ${indexContext.name}">
				${indexContext.name}
			</a>
		</td>
		<td class="td-content">${indexContext.numDocs}</td>
		<td class="td-content"><fmt:formatNumber 
			value="${indexContext.indexSize / 1000000}" 
			maxFractionDigits="0" /></td>
		<td width="1%">
			<c:set var="open" scope="page" value="${indexContext.index.multiSearcher != null ? 'open' : 'closed'}"/>
			<img alt="Server" src="<c:url value="/images/icons/${open}.gif"/>" title="Server">
		</td>
		<td class="td-content">
			${indexContext.index.multiSearcher != null}
		</td>
		<td class="td-content">${indexContext.maxAge / 60}</td>
		<td class="td-content" nowrap="nowrap">${indexContext.latestIndexTimestamp}</td>
		<td class="td-content" nowrap="nowrap">${indexContext.indexDirectoryPath}/${indexContext.name}</td>
	</tr>
	</c:forEach>
</table>
<br>
<br>
<br>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="7" valign="middle">
			<span class="top-content-header">Servers</span>
		</td>
	</tr>
	<tr>
		<th class="td-content" colspan="2">Server</th>
		<th class="td-content">Working</th>
		<th class="td-content">Action</th>
		<th class="td-content">Index</th>
		<th class="td-content">Indexable</th>
		<th class="td-content">Id</th>
		<th class="td-content">Start time</th>
	</tr>
	<c:forEach var="server" items="${requestScope.servers}">
		<tr>
			<td width="1%">
				<img alt="Server" src="<c:url value="/images/icons/server.gif" />" title="Server">
			</td>
			<td class="td-content" nowrap="nowrap">
				<a href="<c:url value="${server.searchWebServiceUrl}" />"
					style="font-style: italic;" 
					title="${server.searchWebServiceUrl}">
					<c:out value="${server.address}" />
				</a>
			</td>
			<c:set var="running" scope="page" value="${server.action != null && server.action.working ? 'running' : 'stopped'}"/>
			<td class="td-content" width="1%">
				<img alt="Working" src="<c:url value="/images/icons/${running}.gif"/>" title="Working">
			</td>
			<c:if test="${server.action != null}">
				<td class="td-content"><c:out value="${server.action.actionName}" /></td>
				<td class="td-content"><c:out value="${server.action.indexName}" /></td>
				<td class="td-content"><c:out value="${server.action.indexableName}" /></td>
				<td class="td-content"><c:out value="${server.action.idNumber}" /></td>
				<td class="td-content"><c:out value="${server.action.startDate}" /></td>
			</c:if>
		</tr>
		
	</c:forEach>
</table>