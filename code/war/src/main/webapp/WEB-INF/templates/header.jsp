<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container" ng-controller="TypeaheadController">
			<a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</a>
			<a class="brand" href="#top">Ikube</a>
			<security:authorize access="isAuthenticated()">
				<form>
					<ul class="nav">
						
						<input 
							type="text" 
							class="input-small" 
							placeholder="Search..." 
							style="margin-top: 9px;"
							ng-model="$searchStrings"
							typeahead="result for result in doSearch('/ikube/service/search/json/multi/all', 'autocomplete', $viewValue)"
							typeahead-min-length="3" 
							typeahead-wait-ms="250">
						
						<button type="submit" class="btn" style="margin-top: 0px;">Go!</button>
						<li><a href="http://www.ikube.be/site">Documentation</a></li>
						<li>
							<a title="<security:authentication property="authorities" />"href="#">
							<spring:message code="security.logged.in.as" />
							<img src="<c:url value="/images/icons/person_obj.gif" />">
							<security:authentication property="name" />
						</a></li>
						<a href="<spring:url value="/logout" htmlEscape="true" />" title="<spring:message code="security.logout" />" class="btn btn-warning" style="margin-top: 0px;">
							<spring:message code="security.logout" />
						</a>
					</ul>
				</form>
			</security:authorize>
		</div>
	</div>
</div>