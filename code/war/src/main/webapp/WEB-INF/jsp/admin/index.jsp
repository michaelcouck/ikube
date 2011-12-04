<%@ taglib prefix="ikube" uri="http://ikube" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--
	This page is for viewing the index and the fields in the index. 
 -->
<c:set var="renderedIndexables"  value="${ikube:add(null, null)}" scope="session" />
<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">Index</span>
			&nbsp;<c:out value="${server.address}" />
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr><th>${indexContext.name}</th></tr>
</table>

<c:set var="children" value="${indexContext.indexables}" scope="session" />
<table width="100%">
	<tr>
		<th>Name</th>
		<th>Class</th>
		<th>Address</th>
		<th>Stored</th>
		<th>Analysed</th>
		<th>Vectored</th>
	</tr>
	<jsp:include page="/WEB-INF/jsp/admin/indexable.jsp" />
</table>

<table width="100%">
	<tr>
		<td class="td-content">
			<strong>index</strong>&nbsp;
			This is the individual index properties and fields page.
		</td>
	</tr>
</table>