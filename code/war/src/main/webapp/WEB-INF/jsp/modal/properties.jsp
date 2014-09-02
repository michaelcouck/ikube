<%--suppress ALL --%>
<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<div id="properties-modal" class="black-box modal hide fade" ng-controller="PropertiesController">
	<div class="modal-header tab-header">
		<button type="button" class="close" data-dismiss="modal">&times;</button>
		<span>Static properties</span>
	</div>
	<div class="modal-body separator">
		<h4>Set the server/cluster properties</h4>
		<div class="input" ng-model="propertyFiles" ng-repeat="(key, value) in propertyFiles">
			<textarea ng-model="propertyFiles[key]" update-model-on-blur cols="450" rows="10" style="width: 95%;"></textarea>
		</div>
	</div>
	<div class="modal-footer">
		<div class="inner-well">
			<a class="button mini rounded light-gray" data-dismiss="modal">Close</a>
			<security:authorize access="hasRole('ROLE_ADMIN')">
				<a class="button mini rounded blue" ng-click="setProperties();">Save changes</a>
			</security:authorize>
		</div>
	</div>
</div>

