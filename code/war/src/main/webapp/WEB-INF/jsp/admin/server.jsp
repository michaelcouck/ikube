<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="2">
			<span class="top-content-header">Server</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>

	<tr>
		<th class="td-content" colspan="2"><a href="javascript:location.reload(true)">Refresh this page</a></th>
	</tr>
	<tr>
		<th class="td-content">Attribute</th>
		<th class="td-content">Attribute Value</th>
	</tr>
	
	<tr>
		<td class="td-content">Address</td>
		<td class="td-content"><c:out value="${requestScope.server.address}" /></td>
	</tr>
	
	<tr>
		<td class="td-content">Working</td>
		<td class="td-content"><c:out value="${requestScope.server.working}" /></td>
	</tr>
	<tr>
		<td class="td-content">Web service urls</td>
		<td class="td-content">
			<c:forEach var="webServiceUrl" items="${requestScope.webServiceUrls}">
				<a href="<c:out value="${webServiceUrl}" />">
					<c:out value="${webServiceUrl}" />
				</a><br>
			</c:forEach>
		</td>
	</tr>
	<tr>
		<td class="td-content">Actions</td>
		<td class="td-content">
			<c:forEach var="action" items="${requestScope.actions}">
				indexable: <c:out value="${action.indexName}" />, 
				name: <c:out value="${action.indexableName}" />, 
				id number: <c:out value="${action.idNumber}" />, 
				start time: <c:out value="${action.startTime}" /><br>
			</c:forEach>
		</td>
	</tr>
	
	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>
	<tr>
		<th class="td-content">Index</th>
		<th class="td-content">Attributes</th>
	</tr>
	
	<c:forEach var="indexContext" items="${requestScope.indexContexts}">
	<tr>
		<td class="td-content">
			<a href="<c:url value="/admin/search.html"/>?address=${requestScope.server.address}&indexName=${indexContext.indexName}">${indexContext.indexName}</a>
		</td>
		<td class="td-content">
			documents: ${requestScope[indexContext.indexName]['indexDocuments']},<br>
			size: ${requestScope[indexContext.indexName]['indexSize']},<br>
			max age: ${indexContext.maxAge},<br>
			batch size: ${indexContext.batchSize},<br>
			internet batch size: ${indexContext.internetBatchSize},<br> 
			max read length: ${indexContext.maxReadLength},<br>
			index directory path: ${indexContext.indexDirectoryPath},<br> 
			index directory path backup: ${indexContext.indexDirectoryPathBackup},<br>
			
			<b>Search performance:</b><br>
			${requestScope.searchingExecutions[indexContext.indexName]}<br>
			
			<b>Indexing performance:</b><br>
			${requestScope.indexingExecutions[indexContext.indexName]}
		</td>
	</tr>
	</c:forEach>
	
	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>
	<tr>
		<td class="bottom-content" colspan="2">Single server details brought to you by Ikube : </td>
	</tr>
</table>