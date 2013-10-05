<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="colspan" value="10" />
<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="${colspan}" valign="middle">
			<span class="top-content-header"><spring:message code="actions" /></span>
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
		<th class="td-content">Id</th>
		<th class="td-content">Action</th>
		<th class="td-content">Indexable</th>
		<th class="td-content">Index name</th>
		<th class="td-content">Start</th>
		<th class="td-content">End</th>
		<th class="td-content">Duration</th>
		<th class="td-content">Invocations</th>
	</tr>
	<c:forEach var="action" items="${actions}">
	<tr>
		<c:set var="text" value="Indexable ${action.indexableName}" />
		<td width="1" nowrap="nowrap">
			<img alt="${text}" src="<c:url value="/img/icons/register_view.gif" />" title="${text}">
		</td>
		<td class="td-content">${action.id}</td>
		<td class="td-content">${action.actionName}</td>
		<td class="td-content">${action.indexableName}</td>
		<td class="td-content">${action.indexName}</td>
		<td class="td-content">
			<fmt:formatDate value="${action.startTime}" pattern="dd/MM HH:mm:ss" type="DATE" />
		</td>
		<td class="td-content">
			<fmt:formatDate value="${action.endTime}" pattern="dd/MM HH:mm:ss" type="DATE" />
		</td>
		<td class="td-content">${action.duration / 1000}</td>
		<td class="td-content">${action.invocations}</td>
	</tr>
	</c:forEach>
	<tr>
		<td colspan="${colspan}">
			<c:set var="block" value="10" />
			<c:set var="total" value="${total > 100 ? 100 : total}"></c:set>
			<c:forEach var="index" begin="0" end="${total}" varStatus="counter"> 
				<c:if test="${index % block == 0}">
					<a href="<c:url value="/admin/actions.html" />?start=${index}&end=${block}">${index}</a>
				</c:if>
			</c:forEach> 
		</td>
	</tr>
</table>