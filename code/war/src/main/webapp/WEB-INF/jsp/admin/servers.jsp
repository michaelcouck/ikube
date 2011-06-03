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
		<th class="td-content">Server</th>
		<th class="td-content">Working</th>
		<th class="td-content">Indexable</th>
		<th class="td-content">Name</th>
		<th class="td-content">Id number</th>
		<th class="td-content">Start time</th>
	</tr>
	<c:forEach var="server" items="${requestScope.servers}">
		<tr>
			<td>
				<span title="${server.searchWebServiceUrl}" 
					style="font-size: smaller; font-style: italic; font-weight: bolder;">
				<c:out value="${server.address}" />
				</span>
			</td>
			<td><c:out value="${server.working}" /></td>
			<td colspan="4"></td>
		</tr>
	
		<c:forEach var="action" items="${server.actions}">
			<tr>
				<td colspan="2"></td>
				<td><c:out value="${action.indexName}" /></td>
				<td><c:out value="${action.indexableName}" /></td>
				<td><c:out value="${action.idNumber}" /></td>
				<td><c:out value="${action.startDate}" /></td>
			</tr>
		</c:forEach>
	
		<tr>
			<td colspan="2">
				<span style="font-style: italic;">
					Indexing performance
				</span>
			</td>
			<th>Name</th>
			<th>Duration</th>
			<th>Executions</th>
			<th>Per second</th>
		</tr>
	
		<c:forEach var="execution" items="${server.indexingExecutions}">
			<tr>
				<td colspan="2"></td>
				<td><c:out value="${execution.key}" /></td>
				<td><fmt:formatNumber value="${execution.value.duration / 1000000000 / 60}" maxFractionDigits="2" /></td>
				<td><c:out value="${execution.value.invocations}" /></td>
				<td colspan="2"><c:out value="${execution.value.executionsPerSecond}" /></td>
			</tr>
		</c:forEach>
	
		<tr>
			<td colspan="2">
				<span style="font-style: italic;">
					Searching performance
				</span>
			</td>
			<th>Name</th>
			<th>Duration</th>
			<th>Executions</th>
			<th>Per second</th>
		</tr>
	
		<c:forEach var="execution" items="${server.searchingExecutions}">
			<tr>
				<td colspan="2"></td>
				<td><c:out value="${execution.key}" /></td>
				<td><fmt:formatNumber value="${execution.value.duration / 1000000000 / 60}" maxFractionDigits="2" /></td>
				<td><c:out value="${execution.value.invocations}" /></td>
				<td colspan="2"><c:out value="${execution.value.executionsPerSecond}" /></td>
			</tr>
		</c:forEach>
		
		<tr>	
			<td colspan="6">&nbsp;</td>
		</tr>
	</c:forEach>
</table>
<br>

<table class="table-content" width="100%">	
	<tr>
		<th class="td-content">Index</th>
		<th class="td-content">Docs</th>
		<th class="td-content">Size</th>
		<th class="td-content">Max age</th>
		<th class="td-content">Batch size</th>
		<th class="td-content">Inet batch size</th>
		<th class="td-content">Read length</th>
		<th class="td-content">Index path</th>
		<th class="td-content">Backup path</th>
	</tr>
	
	<c:forEach var="indexContext" items="${requestScope.indexContexts}">
	<tr>
		<td>
			<span>
			<a href="<c:url value="/admin/search.html"/>?indexName=${indexContext.indexName}" 
				style="font-style: italic;" 
				title="Search index ${indexContext.indexName}">
				${indexContext.indexName}
			</a>
			</span>
		</td>
		<td>${requestScope[indexContext.indexName]['indexDocuments']}</td>
		<td>${requestScope[indexContext.indexName]['indexSize'] / 1000000}</td>
		<td>${indexContext.maxAge / 1000 / 60 / 60}</td>
		<td>${indexContext.batchSize}</td>
		<td>${indexContext.internetBatchSize}</td>
		<td>${indexContext.maxReadLength / 1000000}</td>
		<td>${indexContext.indexDirectoryPath}</td>
		<td>${indexContext.indexDirectoryPathBackup}</td>
	</tr>
	</c:forEach>
	
	<tr>
		<td class="bottom-content" colspan="11">Servers' details brought to you by Ikube : </td>
	</tr>
</table>