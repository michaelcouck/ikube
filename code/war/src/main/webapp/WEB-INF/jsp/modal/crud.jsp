<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<div id="crud-modal" class="black-box modal hide fade">
	<div class="modal-header tab-header">
		<button type="button" class="close" data-dismiss="modal">&times;</button>
		<span>Edit entity</span>
	</div>
	<div class="modal-body separator">
		<h4>Editing entity {{'bla...'}}</h4>
		<div class="input">
			<h5>Bla... :</h5>
			<div class="user-content">
				<label>{{key}}</label><input type="text" class="input-transparent" value="{{value}}" />
			</div>
		</div>
	</div>
	<div class="modal-footer">
		<div class="inner-well">
			<div class="btn-group">
				<button class="button black mini" ng-click="update();"><i class="icon-pencil"></i>Update</button>
				<button class="button black mini" ng-click="cancel();"><i class="icon-remove"></i>Cancel</button>
				<button class="button black mini" ng-click="parent();"><i class="icon-ok"></i>Parent</button>
			</div>
			<a class="button mini rounded light-gray" data-dismiss="modal">Close</a>
		</div>
	</div>
</div>