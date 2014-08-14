<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="apis-modal" class="black-box modal hide fade" ng-controller="ApisController">
	<div class="modal-header tab-header">
		<button type="button" class="close" data-dismiss="modal">&times;</button>
		<span>Rest API endpoints</span>
	</div>
	<div class="modal-body separator">
        <div class="input" ng-repeat="api in apis">
            <h6>{{api.description}}</h6>
            <div class="input" ng-repeat="apiMethod in api.apiMethods" style="padding-left: 15px;">
                <a href="#" ng-click="collapse(apiMethod.uri)">{{apiMethod.method}} : /ikube/{{apiMethod.uri}}</a>
                <div ng-show="collapsed[apiMethod.uri]">
                    {{apiMethod.description}}
                    Consumes:<br>
                    <textarea rows="10" cols="250">{{apiMethod.consumes}}</textarea>
                    Produces:<br>
                    <textarea rows="10" cols="250">{{apiMethod.produces}}</textarea>
                </div>
                <br>
            </div>
            <hr>
        </div>
	</div>
	<div class="modal-footer">
		<div class="inner-well">
			<a class="button mini rounded light-gray" data-dismiss="modal">Close</a>
		</div>
	</div>
</div>