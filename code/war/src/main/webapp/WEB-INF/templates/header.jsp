<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<c:url var="documentation" value="/documentation/index.html" />

<div id="header" ng-controller="ServersController">
	<a href="<c:url value="/index.html" />" >ikube</a>
	<security:authorize access="isAuthenticated()">
		<div style="float : right; padding-right : 10px;">
			<a href="http://www.ikube.be/site">Documentation</a>&nbsp;
			<a title="<security:authentication property="authorities" />" href="#">
				<spring:message code="security.logged.in.as" />
				<img src="<c:url value="/images/icons/person_obj.gif" />">
				<security:authentication property="name" /> 
			</a>
			<a href="<spring:url value="/logout" htmlEscape="true" />" title="<spring:message code="security.logout" />"><spring:message code="security.logout" /></a>
		</div>
	</security:authorize>
</div>