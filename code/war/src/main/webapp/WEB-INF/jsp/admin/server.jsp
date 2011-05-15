<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="2">
			<span class="top-content-header">Server</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>

	<tr>
		<th class="td-content">Index</th>
		<th class="td-content">Attributes</th>
	</tr>

	<c:forEach var="indexName" items="${requestScope.indexNames}">
	<tr>
		<td class="td-content">
			<a href="<c:url value="/admin/search.html"/>?address=${requestScope.server.address}&indexName=${indexName}">${indexName}</a>
		</td>
		<td class="td-content">
			documents: xxx, size: xxx 
		</td>
	</tr>
	</c:forEach>

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
		<th class="td-content">Context</th>
		<th class="td-content">Attributes</th>
	</tr>
	
	<c:forEach var="indexContext" items="${requestScope.indexContexts}">
	<tr>
		<td class="td-content"><c:out value="${indexContext.indexName}" /></td>
		<td class="td-content">
			max age: <c:out value="${indexContext.maxAge}" />,  
			batch size: <c:out value="${indexContext.batchSize}" />, 
			internet batch size: <c:out value="${indexContext.internetBatchSize}" />, 
			max read length: <c:out value="${indexContext.maxReadLength}" />, 
			index directory path: <c:out value="${indexContext.indexDirectoryPath}" />, 
			index directory path backup: <c:out value="${indexContext.indexDirectoryPathBackup}" />
		</td>
	</tr>
	</c:forEach>
	
	<tr>
		<td class="bottom-content" colspan="2">Single server details brought to you by Ikube : </td>
	</tr>
</table>