<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="6" valign="middle">
			<span class="top-content-header">Servers & indexes</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	
	<tr>
		<th>Server</th>
		<th>Working</th>
		<th>Indexable</th>
		<th>Name</th>
		<th>Id number</th>
		<th>Start time</th>
	</tr>
	<c:forEach var="server" items="${requestScope.servers}">
	<tr>
		<td><c:out value="${server.address}" /></td>
		<td><c:out value="${server.working}" /></td>
		<td></td>
		<td></td>
		<td></td>
		<td></td>
	</tr>
	
	<c:forEach var="action" items="${server.actions}">
	<tr>
		<td></td>
		<td></td>
		<td><c:out value="${action.indexName}" /></td>
		<td><c:out value="${action.indexableName}" /></td>
		<td><c:out value="${action.idNumber}" /></td>
		<td><c:out value="${action.startTime}" /></td>
	</tr>
	</c:forEach>

	</c:forEach>
	
</table>
<br>

<table class="table-content" width="100%">	
	<tr>
		<th class="td-content">Name</th>
		<th class="td-content">Docs</th>
		<th class="td-content">Size</th>
		<th class="td-content">Max age</th>
		<th class="td-content">Batch size</th>
		<th class="td-content">Inet batch size</th>
		<th class="td-content">Read length</th>
		<th class="td-content">Index path</th>
		<th class="td-content">Backup path</th>
		<th class="td-content">Search perf.</th>
		<th class="td-content">Indexing perf.</th>
	</tr>
	
	<c:forEach var="indexContext" items="${requestScope.indexContexts}">
	<tr>
		<td>
			<a href="<c:url value="/admin/search.html"/>?address=${requestScope.server.address}&indexName=${indexContext.indexName}">${indexContext.indexName}</a>
		</td>
		<td>${requestScope[indexContext.indexName]['indexDocuments']}</td>
		<td>${requestScope[indexContext.indexName]['indexSize'] / 1000000}</td>
		<td>${indexContext.maxAge / 1000 / 60 / 60}</td>
		<td>${indexContext.batchSize}</td>
		<td>${indexContext.internetBatchSize}</td>
		<td>${indexContext.maxReadLength / 1000000}</td>
		<td>${indexContext.indexDirectoryPath}</td>
		<td>${indexContext.indexDirectoryPathBackup}</td>
		<td>${requestScope.searchingExecutions[indexContext.indexName]}</td>
		<td>${requestScope.indexingExecutions[indexContext.indexName]}</td>
	</tr>
	</c:forEach>
	
	<tr>
		<td class="bottom-content" colspan="11">Servers' details brought to you by Ikube : </td>
	</tr>
</table>