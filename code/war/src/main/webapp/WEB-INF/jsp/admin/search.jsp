<%@ page import="java.util.Map"%>
<%@ page import="ikube.model.IndexContext"%>
<%@ page import="ikube.toolkit.GeneralUtilities"%>
<%@ page import="ikube.cluster.IClusterManager"%>
<%@ page import="ikube.toolkit.ApplicationContextManager"%>
<%@ page import="ikube.monitoring.IMonitoringService"%>
<%@ page import="ikube.model.Server"%>
<%@ page import="java.util.List"%>
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
		// TODO This can all get set in the front controller
		String address = request.getParameter("address");
		List<Server> servers = ApplicationContextManager.getBean(IClusterManager.class).getServers();
		Server server = GeneralUtilities.findObject(Server.class, servers, "address", address);
		pageContext.setAttribute("server", server);
		String[] indexNames = ApplicationContextManager.getBean(IMonitoringService.class).getIndexNames();
		pageContext.setAttribute("indexNames", indexNames);
	%>
	
	<tr>
		<td class="td-content">Server</td>
		<td class="td-content">${param.address}</td>
	</tr>
	
	<c:forEach var="indexName" items="${pageScope.indexNames}">
	<tr>
		<td class="td-content">${indexName}</td>
		<td class="td-content" style="text-align: right; float: right;">
			<ul>
				<li id="search">
					<form name="searchForm" id="${indexName}" action="<c:url value="/admin/search.html"/>">
						<fieldset>
							<input type="hidden" name="address" value="${param.address}">
							<input type="hidden" name="indexName" value="${indexName}">
							<input type="hidden" name="fragment" value="true">
							<!-- TODO Need to add all the fields in the index in the parameter list as searchFields -->
							<input type="hidden" name="searchFields" value="content">
							<!-- 
								TODO This must be the specific server, i.e. ${server.ip}. Somehow this goes to 
								192.168.1.137 which is directed somewhere else, perhaps by the router? For now 
								we stick with the localhost 
							-->
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