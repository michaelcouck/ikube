<%@ taglib prefix="ikube" uri="http://ikube" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--
	This page is for viewing the index and the fields in the index. 
 -->
<c:set var="renderedIndexables"  value="${ikube:add(null, null)}" scope="session" />
<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="2">
			<span class="top-content-header">Index</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr><th colspan="2">${indexContext.name}</th></tr>
	
	<tr>
		<td>
			<c:set var="children" value="${indexContext.indexables}" scope="session" />
			<jsp:include page="/WEB-INF/jsp/admin/indexable.jsp" />
		</td>
	</tr>
</table>

<table>
	<tr>
		<td class="td-content" colspan="2">
			<strong>index</strong>&nbsp;
			This is the individual index properties and fields page.
		</td>
	</tr>
</table>