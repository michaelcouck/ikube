<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<div id="header" class="header">
	<table width="100%" border="0">
		<tr>
			<td width="50%">
				<h1><a href="<c:url value="/index.html" />">Ikube</a></h1>
			</td>
			<td width="50%" style="float : right;" nowrap="nowrap" align="right">
				<form id="search-form" name="search-form" action="<c:url value="/results.html" />">
					<div ng-controller='AutoCompleteController'>
						<input 
							auto-complete 
							ui-items="names" 
							ng-model="selected"
							id="searchString" 
							name="searchString" 
							value="${param.searchStrings}" 
							width="150px">
							<input type="submit" value="Go!">
					</div>
					<div ng-controller="SearcherController">
						<span ng-hide="!statistics.corrections">
							<script type="text/javascript">
								function setSearchStringAndSubmit() {
									var searchForm = document.getElementById('search-form');
									searchForm.value = $scope.statistics.corrections;
									searchForm.submit();
								}
							</script>
							Did you mean : 
							<a onclick="JavaScript:setSearchStringAndSubmit();">
								{{statistics.corrections}}
							</a>
						</span>
					</div>
				</form>
			</td>
			<td nowrap="nowrap">
				<security:authorize access="isAuthenticated()">
					<a title="<security:authentication property="authorities" />" href="#">
						<spring:message code="security.logged.in.as" />&nbsp;<img src="<c:url value="/images/icons/person_obj.gif" />">&nbsp;<security:authentication property="name" /> 
					</a>
				<a href="<spring:url value="/logout" htmlEscape="true" />" title="<spring:message code="security.logout" />"><spring:message code="security.logout" /></a>
				</security:authorize>
			</td>
		</tr>
	</table>
</div>