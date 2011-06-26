<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="6" valign="middle">
			<span class="top-content-header">Servers & indexes</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>	
	<tr>
		<th class="td-content">Index</th>
		<th class="td-content">Docs</th>
		<th class="td-content">Size</th>
		<th class="td-content">Open</th>
		<th class="td-content">Max age</th>
		<th class="td-content">Timestamp</th>
		<th class="td-content">Index path</th>
	</tr>
	
	<c:forEach var="indexContext" items="${requestScope.indexContexts}">
	<tr>
		<td class="td-content">
			<img alt="Search index ${indexContext.name}" 
					src="<c:url value="/images/icons/search.gif"/>" 
					title="Search index ${indexContext.name}">
			<a href="<c:url value="/admin/search.html"/>?indexName=${indexContext.name}" 
				style="font-style: italic;" 
				title="Search index ${indexContext.name}">
				${indexContext.name}
			</a>
		</td>
		<td class="td-content">${indexContext.numDocs}</td>
		<td class="td-content"><fmt:formatNumber 
			value="${indexContext.indexSize / 1000000}" 
			maxFractionDigits="0" /></td>
		<td class="td-content">
			<c:set var="open" scope="page" value="${indexContext.index.multiSearcher != null ? 'open' : 'closed'}"/>
			<img alt="Server" src="<c:url value="/images/icons/${open}.gif"/>" title="Server">
			${indexContext.index.multiSearcher != null}
		</td>
		<td class="td-content">${indexContext.maxAge / 60}</td>
		<td class="td-content">${indexContext.latestIndexTimestamp}</td>
		<td class="td-content">${indexContext.indexDirectoryPath}/${indexContext.name}</td>
	</tr>
	</c:forEach>
</table>
<br>

<table class="table-content" width="100%">
	<tr>
		<th class="td-content">Server</th>
		<th class="td-content">Working</th>
		<th class="td-content">Action</th>
		<th class="td-content">Index</th>
		<th class="td-content">Indexable</th>
		<th class="td-content">Id</th>
		<th class="td-content">Start time</th>
	</tr>
	<c:forEach var="server" items="${requestScope.servers}">
		<tr>
			<td class="td-content">
				<img alt="Server" src="<c:url value="/images/icons/server.gif"/>" title="Server">
				<a href="<c:url value="${server.searchWebServiceUrl}" />"
					style="font-style: italic;" 
					title="${server.searchWebServiceUrl}">
					<c:out value="${server.address}" />
				</a>
			</td>
			<td class="td-content">
				<c:set var="running" scope="page" value="${server.working ? 'running' : 'stopped'}"/>
				<img alt="Working" src="<c:url value="/images/icons/${running}.gif"/>" title="Working">
				<c:out value="${server.working}" />
			</td>
			<td colspan="5"></td>
		</tr>
		
		<c:if test="${server.action != null}">
			<tr>
				<td colspan="2"></td>
				<td class="td-content"><c:out value="${server.action.actionName}" /></td>
				<td class="td-content"><c:out value="${server.action.name}" /></td>
				<td class="td-content"><c:out value="${server.action.indexableName}" /></td>
				<td class="td-content"><c:out value="${server.action.idNumber}" /></td>
				<td class="td-content"><c:out value="${server.action.startDate}" /></td>
			</tr>
		</c:if>
	
		<tr>
			<td colspan="3" style="padding-left: 20px;">
				<img alt="Index performance" 
					src="<c:url value="/images/icons/index_performance.gif"/>" 
					title="Index performance">
				<span style="font-style: italic;">
					Indexing performance:
				</span>
			</td>
			<th>Index</th>
			<th>Duration</th>
			<th>Executions</th>
			<th>Per second</th>
		</tr>
	
		<c:forEach var="execution" items="${server.indexingExecutions}">
			<tr>
				<td colspan="3"></td>
				<td class="td-content"><c:out value="${execution.key}" /></td>
				<td class="td-content"><fmt:formatNumber value="${execution.value.duration / 1000000000 / 60}" maxFractionDigits="2" /></td>
				<td class="td-content"><c:out value="${execution.value.invocations}" /></td>
				<td colspan="2"><c:out value="${execution.value.executionsPerSecond}" /></td>
			</tr>
		</c:forEach>
	
		<tr>
			<td colspan="3" style="padding-left: 20px;">
				<img alt="Search performance" 
					src="<c:url value="/images/icons/search_performance.gif"/>" 
					title="Search performance">
				<span style="font-style: italic;">
					Searching performance:
				</span>
			</td>
			<th>Index</th>
			<th>Duration</th>
			<th>Executions</th>
			<th>Per second</th>
		</tr>
	
		<c:forEach var="execution" items="${server.searchingExecutions}">
			<tr>
				<td colspan="3"></td>
				<td class="td-content"><c:out value="${execution.key}" /></td>
				<td class="td-content"><fmt:formatNumber value="${execution.value.duration / 1000000000 / 60}" maxFractionDigits="2" /></td>
				<td class="td-content"><c:out value="${execution.value.invocations}" /></td>
				<td colspan="2"><c:out value="${execution.value.executionsPerSecond}" /></td>
			</tr>
		</c:forEach>
		
		<tr>	
			<td colspan="7">&nbsp;</td>
		</tr>
	</c:forEach>
</table>
