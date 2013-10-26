<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<div id="logs-modal" class="black-box modal hide fade" ng-controller="ServersController">
	<div class="modal-header tab-header">
		<button type="button" class="close" data-dismiss="modal">&times;</button>
		<span>Server logs</span>
	</div>
	<div class="modal-body separator">
		<h4>Server logs for the cluster</h4>
		<div class="input" ng-repeat="server in servers">
			<h5>Server : {{server.address}}</h5>
			<textarea cols="450" rows="10" style="width: 100%;">{{server.logTail}}</textarea>
		</div>
	</div>
	<div class="modal-footer">
		<div class="inner-well">
			<a class="button mini rounded light-gray" data-dismiss="modal">Close</a>
		</div>
	</div>
</div>