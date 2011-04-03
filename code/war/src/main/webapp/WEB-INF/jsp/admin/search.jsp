<%@page import="ikube.monitoring.IMonitoringService"%>
<%@page import="java.util.Map"%>
<%@page import="ikube.model.IndexContext"%>
<%@page import="ikube.toolkit.GeneralUtilities"%>
<%@page import="ikube.cluster.IClusterManager"%>
<%@page import="ikube.toolkit.ApplicationContextManager"%>
<%@page import="ikube.model.Server"%>
<%@page import="java.util.List"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="search" uri="http://ikube/search" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="2">
			<span class="top-content-header">Search</span>
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
		<td class="td-content">Server</td>
		<td class="td-content">${pageScope.server.address}</td>
	</tr>
	
	<c:forEach var="indexContext" items="${pageScope.indexContexts}">
		<tr>
			<td class="td-content">${indexContext.indexName}</td>
			<td class="td-content" style="text-align: right; float: right;">
				<ul>
					<li id="search">
						<form name="searchForm" id="${indexContext.indexName}" action="<c:url value="/admin/search.html"/>">
							<fieldset>
								<input type="hidden" name="address" value="${requestScope.address}">
								<input type="hidden" name="indexName" value="${indexContext.indexName}">
								<input type="hidden" name="fragment" value="true">
								<!-- TODO Need to add all the fields in the index in the parameter list as searchFields -->
								<input type="hidden" name="searchFields" value="content">
								<!-- TODO Need to add this url dynamically somehow -->
								<input type="hidden" name="searchUrl" value="http://localhost:9000/ikube/SearchServlet">
								<input type="text" name="searchStrings" id="search-text-plain" value="${param.searchStrings}" />
								<input type="submit" id="search-submit-plain" value="Go" />
							</fieldset>
						</form>
					</li>
				</ul>
			</td>
		</tr>
	</c:forEach>
	
	<tr>
		<td class="td-content" colspan="2">
			&nbsp;
		</td>
	</tr>
	
	<tr>
		<th class="td-content" colspan="2">Results</th>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			<table>
				<jsp:include page="/WEB-INF/jsp/results.jspf" flush="true" />
			</table>
		</td>
	</tr>
	
	<tr>
		<td class="td-content" colspan="2">
			<strong>configuration</strong>&nbsp;
			This is the search page for individual servers. On this page there is one server defined. For this server there 
			are several indexes defined. You can search the individual indexes on this specific server using the search box. 
			There may be several fields defined in the indexes which can be searched separately but for convenience all 
			the fields are added to the fields to search, the result of which is that all the fields in the index will be searched 
			with the search string in the text field.
		</td>
	</tr>
		
	<tr>
		<td class="bottom-content">Search per server and index</td>
	</tr>
</table>