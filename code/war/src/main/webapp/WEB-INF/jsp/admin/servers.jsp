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
			<td class="td-content">
				<a href="<c:url value="${server.searchWebServiceUrl}" />"
					style="font-style: italic;" 
					title="${server.searchWebServiceUrl}">
					<c:out value="${server.address}" />
				</a>
			</td>
			<td class="td-content"><c:out value="${server.working}" /></td>
			<td colspan="4"></td>
		</tr>
		
		<!-- 
			We'll use a counter to limit the number of actions that are displayed on the
			page as there can be several servers and many actions making the page very long. 
		-->
		<%-- <c:set var="counter" scope="page" value="server.actions"/> --%>
		<c:forEach var="action" items="${server.actions}">
			<%-- <c:set var="counter" scope="page" value="${counter - 1}"/> --%>
			<%-- <c:if test="${counter < 10}"></c:if> --%>
			<tr>
				<td colspan="2"></td>
				<td class="td-content"><c:out value="${action.indexName}" /></td>
				<td class="td-content"><c:out value="${action.indexableName}" /></td>
				<td class="td-content"><c:out value="${action.idNumber}" /></td>
				<td class="td-content"><c:out value="${action.startDate}" /></td>
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
				<td class="td-content"><c:out value="${execution.key}" /></td>
				<td class="td-content"><fmt:formatNumber value="${execution.value.duration / 1000000000 / 60}" maxFractionDigits="2" /></td>
				<td class="td-content"><c:out value="${execution.value.invocations}" /></td>
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
				<td class="td-content"><c:out value="${execution.key}" /></td>
				<td class="td-content"><fmt:formatNumber value="${execution.value.duration / 1000000000 / 60}" maxFractionDigits="2" /></td>
				<td class="td-content"><c:out value="${execution.value.invocations}" /></td>
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
		<th class="td-content">Open</th>
		<th class="td-content">Max age</th>
		<th class="td-content">Batch size</th>
		<th class="td-content">Inet batch size</th>
		<th class="td-content">Read length</th>
		<th class="td-content">Index path</th>
		<th class="td-content">Backup path</th>
	</tr>
	
	<c:forEach var="indexContext" items="${requestScope.indexContexts}">
	<tr>
		<td class="td-content">
			<a href="<c:url value="/admin/search.html"/>?indexName=${indexContext.indexName}" 
				style="font-style: italic;" 
				title="Search index ${indexContext.indexName}">
				${indexContext.indexName}
			</a>
		</td>
		<td class="td-content">${requestScope[indexContext.indexName]['indexDocuments']}</td>
		<td class="td-content"><fmt:formatNumber 
			value="${requestScope[indexContext.indexName]['indexSize'] / 1000000}" 
			maxFractionDigits="0" /></td>
		<td class="td-content">${indexContext.index.multiSearcher != null}</td>
		<td class="td-content">${indexContext.maxAge / 60}</td>
		<td class="td-content">${indexContext.batchSize}</td>
		<td class="td-content">${indexContext.internetBatchSize}</td>
		<td class="td-content">${indexContext.maxReadLength / 1000000}</td>
		<td class="td-content">${indexContext.indexDirectoryPath}</td>
		<td class="td-content">${indexContext.indexDirectoryPathBackup}</td>
	</tr>
	</c:forEach>
	
	<tr>
		<td class="bottom-content" colspan="11">Servers' details brought to you by Ikube : </td>
	</tr>
</table>