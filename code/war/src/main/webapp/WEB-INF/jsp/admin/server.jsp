<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="ikube.model.IndexContext"%>
<%@ page import="java.util.Map"%>
<%@ page import="ikube.toolkit.GeneralUtilities"%>
<%@ page import="java.util.Collections"%>
<%@ page import="ikube.model.Server.Action"%>
<%@ page import="ikube.model.Server"%>
<%@ page import="java.util.List"%>
<%@ page import="ikube.cluster.IClusterManager"%>
<%@ page import="ikube.toolkit.ApplicationContextManager"%>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="2">
			<span class="top-content-header">Server</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>

	<%
		// TODO This can all go in one tag that puts all the data in the page
		String address = request.getParameter("address");
		List<Server> servers = ApplicationContextManager.getBean(IClusterManager.class).getServers();
		Server server = GeneralUtilities.findObject(Server.class, servers, "address", address);
		if (server != null) {
			pageContext.setAttribute("server", server);
			pageContext.setAttribute("actions", server.getActions());
			pageContext.setAttribute("webServiceUrls", server.getWebServiceUrls());
		}
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		pageContext.setAttribute("indexContexts", indexContexts.values());
	%>
	
	<tr>
		<th class="td-content">Attribute</th>
		<th class="td-content">Attribute Value</th>
	</tr>
	
	<tr>
		<td class="td-content">Address</td>
		<td class="td-content"><c:out value="${server.address}" /></td>
	</tr>
	<tr>
		<td class="td-content">Working</td>
		<td class="td-content"><c:out value="${server.working}" /></td>
	</tr>
	<tr>
		<td class="td-content">Web service urls</td>
		<td class="td-content">
			<c:forEach var="webServiceUrl" items="${pageScope.webServiceUrls}">
				<a href="<c:out value="${webServiceUrl}" />">
					<c:out value="${webServiceUrl}" />
				</a><br>
			</c:forEach>
		</td>
	</tr>
	<tr>
		<td class="td-content">Actions</td>
		<td class="td-content">
			<c:forEach var="action" items="${pageScope.actions}">
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
	
	<c:forEach var="indexContext" items="${pageScope.indexContexts}">
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