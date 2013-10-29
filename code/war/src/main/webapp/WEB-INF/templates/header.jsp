<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<script type="text/javascript">
// Focus on the first field in the form
angular.element(document).ready(function() {
	doFocus('search');
});
</script>

	<div class="top-nav" ng-controller="TypeaheadController">
		<div class="container-fluid">
			<div class="row-fluid search-button-bar-container">
				<div class="span12">
					<ul class="breadcrumb">
						<li><a href="#"><i class="icon-home"></i> Some</a></li>
						<li><a href="#">Nice</a></li>
						<li><a href="#">Breadcrumbs</a></li>
						<li class="active"><a href="#">Here</a></li>
						<button class="button mini inset black" title="<security:authentication property="authorities" />" href="#">
							<spring:message code="security.logged.in.as" />
							<img src="<c:url value="/assets/images/icons/person_obj.gif" />">
							<security:authentication property="name" />
						</button>
					</ul>
					<a class="search-button-trigger" href="#"><i
						class="icon-search"></i></a>
				</div>
			</div>

			<div class="row-fluid search-bar-nav">
				<div class="span12">
					<form>
						<input
							id="search"
							type="text"
							class="search" 
							name="search" 
							style="margin-top: 9px;"
							placeholder="Search documentation..."
							
							ng-model="result"
							typeahead="result for result in doSearch('/ikube/service/search/json/complex/sorted/json/all', $viewValue)"
							
							typeahead-min-length="3" 
							typeahead-wait-ms="250"
							typeahead-on-select="doModalResults();">
						
					</form>
				</div>
			</div>
		</div>
	</div>