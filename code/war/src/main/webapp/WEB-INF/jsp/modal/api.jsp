<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="apis-modal" class="black-box modal hide fade" ng-controller="ApisController">
	<div class="modal-header tab-header">
		<button type="button" class="close" data-dismiss="modal">&times;</button>
		<span>Rest API endpoints</span>
	</div>
	<div class="modal-body separator">
        <div class="input" ng-repeat="api in apis">
            <h5>Api : {{api.api}}</h5>
            <div class="input" ng-repeat="apiMethod in api.apiMethods">
                <b>Type:</b> {{apiMethod.type}}<br><br>
                <b>Uri:</b> {{apiMethod.uri}}<br><br>
                <b>Description:</b> {{apiMethod.description}}<br><br>
                <b>Consumes:</b> {{ apiMethod.consumes }} <br><br>
                <b>Produces:</b> {{ apiMethod.produces }} <br><br>
                <br>
            </div>
        </div>

	</div>
	<div class="modal-footer">
		<div class="inner-well">
			<a class="button mini rounded light-gray" data-dismiss="modal">Close</a>
		</div>
	</div>
</div>