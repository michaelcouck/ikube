<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<div id="header">
	<a href="<c:url value="/index.html" />" >ikube</a>
	
	<security:authorize access="isAuthenticated()">
	<a title="<security:authentication property="authorities" />" href="#">
		<spring:message code="security.logged.in.as" /> <security:authentication property="name" /> 
	</a>
	<a href="<spring:url value="/logout" htmlEscape="true" />" title="<spring:message code="security.logout" />"><spring:message code="security.logout" /></a>
	</security:authorize>
</div>

