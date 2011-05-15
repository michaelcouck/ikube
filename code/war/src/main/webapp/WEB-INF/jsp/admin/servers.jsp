<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="4">
			<span class="top-content-header">Servers</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	
	<tr>
		<th class="td-content" colspan="4"><a href="javascript:location.reload(true)">Refresh this page</a></th>
	</tr>
	<tr>
		<th class="td-content">Address</th>
		<th class="td-content">Is working</th>
		<th class="td-content">Details page</th>
	</tr>
	<c:forEach var="server" items="${requestScope.servers}">
	<tr>
		<td class="td-content"><c:out value="${server.address}" /></td>
		<td class="td-content"><c:out value="${server.working}" /></td>
		<td class="td-content">
			<a href="<c:url value="/admin/server.html"/>?address=${server.address}">Details</a>
		</td>
	</tr>
	</c:forEach>
	
	<tr>
		<td class="bottom-content">Servers' details brought to you by Ikube : </td>
	</tr>
</table>