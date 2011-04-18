<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ page import="ikube.model.Server.Action"%>
<%@ page import="ikube.model.Server"%>
<%@ page import="java.util.List"%>
<%@ page import="ikube.cluster.IClusterManager"%>
<%@ page import="ikube.toolkit.ApplicationContextManager"%>
<%@ taglib prefix="search" uri="http://ikube/search" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="4">
			<span class="top-content-header">Servers</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	
	<tr>
		<th class="td-content">Address</th>
		<th class="td-content">Is working</th>
		<th class="td-content">Details page</th>
		<th class="td-content">Search page</th>
	</tr>
	<%
		List<Server> servers = ApplicationContextManager.getBean(IClusterManager.class).getServers();
		pageContext.setAttribute("servers", servers);
	%>
	<c:forEach var="server" items="${pageScope.servers}">
	<tr>
		<td class="td-content"><c:out value="${server.address}" /></td>
		<td class="td-content"><c:out value="${server.working}" /></td>
		<td class="td-content">
			<a href="<c:url value="/admin/server.html"/>?address=${server.address}">Details</a>
		</td>
		<td class="td-content">
			<a href="<c:url value="/admin/search.html"/>?address=${server.address}">Search</a>
		</td>
	</tr>
	</c:forEach>
	
	<tr>
		<td class="bottom-content">Servers' details brought to you by Ikube : </td>
	</tr>
</table>