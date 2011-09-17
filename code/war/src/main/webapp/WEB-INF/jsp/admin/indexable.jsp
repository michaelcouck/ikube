<%@ taglib prefix="ikube" uri="http://ikube" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--
	This snippit is for recursively rendering the indexables. 
 -->
<c:set 
	var="clonedChildren" 
	value="${ikube:clone(children)}" 
	scope="page" />
<c:set 
	var="cleanedClonedChildren" 
	value="${ikube:remove(clonedChildren, renderedIndexables)}" 
	scope="page" />
<table>
	<tr>
		<th>Name</th>
		<th>Class</th>
		<th>Address</th>
		<th>Stored</th>
		<th>Analyzed</th>
		<th>Vectored</th>
	</tr>
	<c:forEach var="child" items="${clonedChildren}">
	<tr>
		<td>${child.name}</td>
		<td>${ikube:className(child)}</td>
		<td>${child.address}</td>
		<td>${child.stored}</td>
		<td>${child.analyzed}</td>
		<td>${child.vectored}</td>
	</tr>
	<c:if test="${child.children != null}">
		<c:set 
			var="renderedIndexables"  
			value="${ikube:add(ikube:clone(renderedIndexables), ikube:clone(clonedChildren))}" 
			scope="session" />
		<c:set 
			var="children" 
			value="${child.children}" 
			scope="session" />
		<jsp:include page="/WEB-INF/jsp/admin/indexable.jsp" flush="true" />
	</c:if>
	</c:forEach>
</table>
