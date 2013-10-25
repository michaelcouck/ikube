<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript">
$(document).ready(function() {
	var forms = document.getElementsByTagName('form');
	setTimeout(function() {
		for ( var i = 0; i < forms.length; i++) {
			$('#' + forms[i].id).ajaxForm(function() {
				alert('Properties updated');
			});
		}
	}, 1000);
});
</script>

<form id="create" name="create" action="<c:url value="/service/database/entity/create" />" method="post">
<table ng-controller="CreateController" style="border : 1px solid #aaaaaa;" width="100%">
	<tr ng-model="entity" ng-repeat="(key, value) in entity" >
		<td>{{key}}</td>
		<td><input type="text" value="{{value}}"></td>
	</tr>
	<tr>
		<td>
			<input type="submit" ng-click="createEntity();" value="Create entity">
		</td>
	</tr>
</table>
</form>