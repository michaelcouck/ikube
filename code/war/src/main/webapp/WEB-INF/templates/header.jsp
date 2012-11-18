<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

Header : 

<security:authorize access="isAuthenticated()">
	<spring:message code="security.logged.in.as" /> : 
	<security:authentication property="name" /> : 
	<security:authentication property="authorities" /> : 
	<a href="<spring:url value="/logout" htmlEscape="true" />" title="<spring:message code="security.logout" />"><spring:message code="security.logout" /></a>
</security:authorize>