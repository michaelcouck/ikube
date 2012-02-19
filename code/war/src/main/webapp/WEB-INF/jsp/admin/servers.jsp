<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!--
	This page displays the servers, the indexes that are defined in the server 
	and the actions that are currently being executed, including the number of
	documents that have been indexed since the time the server started. 
 -->
<c:set var="datePattern" value="dd/MM HH:mm:ss" />
<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="10" valign="middle">
			<span class="top-content-header">Indexes</span>
			&nbsp;<c:out value="${server.address}" />
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr>
		<td colspan="10">
			<span style="float: right;">
				<jsp:include page="/WEB-INF/jsp/admin/refresh.jsp" flush="true" />
			</span>
		</td>
	</tr>
	<tr>
		<th class="td-content" colspan="3">Index</th>
		<th class="td-content">Docs</th>
		<th class="td-content">Size</th>
		<th class="td-content" colspan="2">Open</th>
		<th class="td-content" nowrap="nowrap">Max age</th>
		<th class="td-content">Timestamp</th>
		<th class="td-content" nowrap="nowrap">Index path</th>
	</tr>
	
	<c:forEach var="indexContext" items="${requestScope.indexContexts}">
	<tr>
		<td width="1" nowrap="nowrap">
			<a href="<c:url value="/admin/index.html" />?indexName=${indexContext.name}"
				title="View indexables for ${indexContext.name}">
				<img alt="Search index ${indexContext.name}" 
					src="<c:url value="/images/icons/index.gif" />" 
					title="View indexables for ${indexContext.name}">
			</a>
		</td>
		<td>
			<a href="<c:url value="/admin/search.html" />?indexName=${indexContext.name}"
				title="Search index ${indexContext.name}">
				<img alt="Search index ${indexContext.name}" 
					src="<c:url value="/images/icons/search.gif" />" 
					title="Search index ${indexContext.name}">
			</a>
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
		<td class="td-content">${indexContext.index.multiSearcher != null}</td>
		<td class="td-content">${indexContext.maxAge / 60}</td>
		<td class="td-content" nowrap="nowrap">
			<fmt:formatDate value="${indexContext.latestIndexTimestamp}" pattern="${datePattern}" type="DATE" />
		</td>
		<td class="td-content" nowrap="nowrap">${indexContext.indexDirectoryPath}/${indexContext.name}</td>
	</tr>
	</c:forEach>
</table>
<br>
<br>
<br>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="4" valign="middle">
			<span class="top-content-header">Servers</span>
		</td>
	</tr>
	<tr>
		<th class="td-content" colspan="3" width="25%">Server</th>
		<th class="td-content" width="75%">Actions</th>
	</tr>
	<c:forEach var="server" items="${requestScope.servers}">
	<tr>
		<td  width="1%">
			<c:set var="message" value="Terminate all actions" />
			<a 
				href="<c:url value="/admin/terminate.html?targetView=/admin/servers.html&command=terminate" />" 
				title="${message}">
				<img alt="${message}" src="<c:url value="/images/icons/red_square.gif" />" title="${message}">
			</a>
		</td>
		<td  width="1%">
			<c:set var="message" value="Restart all actions" />
			<a 
				href="<c:url value="/admin/terminate.html?targetView=/admin/servers.html&command=startup" />" 
				title="${message}">
				<img alt="${message}" src="<c:url value="/images/icons/relaunch.gif" />" title="${message}">
			</a>
		</td>
		<td class="td-content" nowrap="nowrap">
			<a href="<c:url value="${server.searchWebServiceUrl}" />"
				style="font-style: italic;" 
				title="${server.address}">
				<c:out value="${server.address}" />
			</a>
		</td>
		
		<td class="td-content">
		<c:choose>
		<c:when test="${fn:length(server.actions) == 0}">
			<table class="table-content" width="100%">
				<tr>
					<th class="td-content" width="10%">Working</th>
					<th class="td-content" width="90%">Message</th>
				</tr>
				<tr>
					<td class="td-content">
						<img alt="Working" src="<c:url value="/images/icons/stopped.gif"/>" title="Not working">
					</td>
					<td class="td-content">
						Not working
					</td>
				</tr>
			</table>
		</c:when>
		<c:otherwise>
			<table class="table-content" width="100%">
				<tr>
					<th class="td-content" width="10%">Work</th>
					<th class="td-content" width="10%">Id</th>
					<th class="td-content" width="10">Action</th>
					<th class="td-content" width="15%">Index</th>
					<th class="td-content" width="15%">Indexable</th>
					<th class="td-content" width="10%">Exec</th>
					<th class="td-content" width="5%">Sec</th>
					<th class="td-content" width="25%">Start time</th>
				</tr>
				<c:forEach var="action" items="${server.actions}">
				<tr>
					<c:set var="running" scope="page" value="${empty action.endTime ? 'running' : 'stopped'}"/>
					<td class="td-content"><img alt="Working" src="<c:url value="/images/icons/${running}.gif"/>" title="Working"></td>
					<td class="td-content"><c:out value="${action.id}" /></td>
					<td class="td-content"><c:out value="${action.actionName}" /></td>
					<td class="td-content"><c:out value="${action.indexName}" /></td>
					<td class="td-content"><c:out value="${action.indexableName}" /></td>
					<td class="td-content"><c:out value="${action.invocations}" /></td>
					<td class="td-content"><c:out value="${action.invocationsPerSecond}" /></td>
					<td class="td-content">
						<fmt:formatDate value="${action.startTime}" pattern="${datePattern}" type="DATE" />
					</td>
				</tr>
				</c:forEach>
			</table>
		</c:otherwise>
		</c:choose>
		</td>
	</tr>
	</c:forEach>
</table>