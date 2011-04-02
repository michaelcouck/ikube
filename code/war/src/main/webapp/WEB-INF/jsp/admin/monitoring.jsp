<%@page import="ikube.model.Server.Action"%>
<%@page import="ikube.model.Server"%>
<%@page import="java.util.List"%>
<%@page import="ikube.cluster.IClusterManager"%>
<%@page import="ikube.toolkit.ApplicationContextManager"%>
<%@ taglib prefix="search" uri="http://ikube/search" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">Monitoring</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	
	<%
		List<Server> servers = ApplicationContextManager.getBean(IClusterManager.class).getServers();
		for (Server server : servers) {
			%>
			<tr>
				<td class="td-content"><%= server.getId() %></td>
				<td class="td-content"><%= server.getIp() %></td>
				<td class="td-content"><%= server.getAddress() %></td>
				<td class="td-content">
				<%
					List<Action> actions = server.getActions();
					for (Action action : actions) {
						%>
							
						<%
					}
				%>
				</td>
			</tr>
			<%
		}
	%>
	
	<tr>
		<td class="bottom-content">Initialise the performance monitor for server : </td>
	</tr>
</table>