<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%@ taglib prefix="ikube" uri="http://ikube" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" valign="middle">
			<span class="top-content-header">Searches</span>
			&nbsp;<c:out value="${server.address}" />
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	
	<tr>
		<td>
			<strong>searches</strong>&nbsp;
			This is the results of the searches that were performed on the indexes in Ikube.
		</td>
	</tr>
	
	<tr>
		<td>
			<span style="float: right;">
				<jsp:include page="/WEB-INF/jsp/admin/refresh.jsp" flush="true" />
			</span>
		</td>
	</tr>
	
	<tr>
		<td width="100%">
			<table width="100%">
				<tr>
					<th class="td-content"></th>
					<c:forEach var="fieldName" items="${fieldNames}">
						<th class="td-content">${fieldName}</th>
					</c:forEach>
				</tr>
				<c:forEach var="entity" items="${entities}">
				<tr>
					<td class="td-content"><img alt="Icon" 
						src="<c:url value="/images/icons/search.gif" />" 
						title="Icon"></td>
					<c:forEach var="fieldName" items="${fieldNames}">
						<td class="td-content">${ikube:fieldValue(fieldName, entity)}</td>
					</c:forEach>
				</tr>
				</c:forEach>
			</table>
		</td>
	</tr>
	
	<tr>
		<td>
			<c:set var="block" value="10" />
			<c:set var="total" value="${total > 100 ? 100 : total}"></c:set>
			<c:forEach var="index" begin="0" end="${total}" varStatus="counter"> 
				<c:if test="${index % block == 0}">
					<a href="<c:url 
						value="/admin/database.html" 
						/>?classType=${param.classType}&start=${index}&end=${block}">${index}</a>
				</c:if>
			</c:forEach> 
		</td>
	</tr>
</table>