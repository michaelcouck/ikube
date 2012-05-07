<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="colspan" value="10" />
<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="${colspan}" valign="middle">
			<span class="top-content-header"><spring:message code="properties" /></span>
			&nbsp;<c:out value="${server.address}" />
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr>
		<td colspan="${colspan}">
			<span style="float: right;">
				<jsp:include page="/WEB-INF/jsp/admin/refresh.jsp" flush="true" />
			</span>
		</td>
	</tr>
	<tr>
		<th class="td-content"><!-- The icon --></th>
		<th class="td-content">Key</th>
		<th class="td-content">Value</th>
	</tr>
	<c:forEach var="property" items="${properties}">
		<tr>
			<td><c:out value="${property.key}" /></td>
			<td><c:out value="${property.value}" escapeXml="false" /><br></td>
		</tr>
	</c:forEach>
	
</table>