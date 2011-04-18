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
				<a href="<c:out value="${webServiceUrl}" /><br>">
					<c:out value="${webServiceUrl}" />
				</a>
				<br>
			</c:forEach>
		</td>
	</tr>
	<tr>
		<td class="td-content">Actions</td>
		<td class="td-content">
			<c:forEach var="action" items="${pageScope.actions}">
				<c:out value="${action.indexName}" /><br>
				<c:out value="${action.indexableName}" /><br>
				<c:out value="${action.idNumber}" /><br>
				<c:out value="${action.startTime}" />
			</c:forEach>
		</td>
	</tr>
	
	<tr>
		<td class="td-content">Index context</td>
		<td class="td-content">
			<c:forEach var="indexContext" items="${pageScope.indexContexts}">
				Name: <c:out value="${indexContext.indexName}" /><br>
				Max age: <c:out value="${indexContext.maxAge}" /><br>
				Batch size: <c:out value="${indexContext.batchSize}" /><br>
			</c:forEach>
		</td>
	</tr>
	
	<tr>
		<td class="bottom-content">Single server details brought to you by Ikube : </td>
	</tr>
</table>