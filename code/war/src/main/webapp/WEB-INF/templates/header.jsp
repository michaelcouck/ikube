<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<div class="top-nav" ng-controller="TypeaheadController" ng-init="doConfig('quickSearchConfig');">
	<div class="container-fluid">
		<div class="row-fluid search-button-bar-container">
			<div class="span12">
				<ul class="breadcrumb">
					<li><a href="<c:url value="/system/dash.html" />"><i class="icon-home"></i>Home</a></li>
                    <li><a href="<c:url value="/analytics/analyze.html" />">Analytics</a></li>
                    <li><a href="<c:url value="/search/search.html" />">Search</a></li>
					<li class="active"><a href="#">Welcome</a></li>
					<button class="button mini inset black" title="<security:authentication property="authorities" />">
						<spring:message code="security.logged.in.as" />
						<img src="<c:url value="/assets/images/icons/person_obj.gif" />">
						<security:authentication property="name" />
					</button>
					<a 
						class="button mini inset black" 
						href="<spring:url value="/logout" htmlEscape="true" />" title="<spring:message code="security.logout" />">
						<i class="icon-off">&nbsp;</i><spring:message code="security.logout" />
					</a>
				<img 
					ng-show="!results" 
					alt="Loading spinner" 
					src="<c:url value="/assets/images/loading.gif" />" 
					height="16px" 
					width="16px" >
				</ul>
				
				<a class="search-button-trigger" href="#"><i class="icon-search"></i></a>
			</div>
		</div>
		<div class="row-fluid search-bar-nav">
			<div class="span8">
				<input
					id="search"
					type="text"
					class="search" 
					name="search" 
					placeholder="Quick search, every field in every index..."
					
					ng-model="searchString"
					typeahead="result for result in doSearch()"
					
					typeahead-min-length="3" 
					typeahead-wait-ms="500">
			</div>
		</div>
	</div>
</div>