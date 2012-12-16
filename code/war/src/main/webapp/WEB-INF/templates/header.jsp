<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<div id="header" ng-controller="StartupController">
	<a href="<c:url value="/index.html" />" >ikube</a>
	
	<security:authorize access="isAuthenticated()">
		<a href="#" ng-click="startupAll();">Start all</a>
		<a href="#" ng-click="terminateAll();">Terminate all</a>
		<div style="float : right; padding-right : 10px;">
			<a title="<security:authentication property="authorities" />" href="#">
				<spring:message code="security.logged.in.as" />
				&nbsp;
				<img src="<c:url value="/images/icons/person_obj.gif" />">
				&nbsp;
				<security:authentication property="name" /> 
			</a>
		<a href="<spring:url value="/logout" htmlEscape="true" />" title="<spring:message code="security.logout" />"><spring:message code="security.logout" /></a>
		</div>
	</security:authorize>
</div>