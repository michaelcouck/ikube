<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<!-- <script type="text/javascript">
$(document).ready(function() {
	var forms = document.getElementsByTagName('form');
	// We have to set the timeout because I think that there are
	// too many threads all trying to nob the dom, and well this one
	// just gets pushed out of the way :()
	setTimeout(function() {
		for ( var i = 0; i < forms.length; i++) {
			// alert('Adding to form : ' + forms[i]);
			$('#' + forms[i].id).ajaxForm(function() {
				alert('Blocking event : ' + forms[i]);
				// Note that there is no logic in here, the jQuery form
				// add on just takes care of all the dirty details, and cancels
				// the browsers natural submit action, leaving the user high and 
				// dry on the page :)
				alert('Properties updated');
			});
		}
	}, 1000);
});
</script> -->

<table ng-controller="PropertiesController" class="table" style="margin-top: 55px;">
	<tr ng-model="propertyFiles" ng-repeat="(key, value) in propertyFiles">
		<td>
			<%-- action="<c:url value="/service/monitor/set-properties" />" --%> 
			<form 
				id="{{$index}}" 
				name="{{$index}}" 
				method="post"
				ng-submit="onSubmit('/ikube/service/monitor/set-properties', this, key, value, $event)">
			<security:authorize access="hasRole('ROLE_ADMIN')">
				<!-- <button id="button-{{$index}}" class="btn btn-small btn-success" type="submit" >Update</button> -->
				<!-- ng-click="onSubmit(this, $event)" --> 
				<input 
					id="button-{{$index}}" 
					name="button-{{$index}}" 
					type="submit" 
					class="btn btn-small btn-success" 
					value="Update">
			</security:authorize>
			<img src="<c:url value="/images/icons/jar_l_obj.gif" />" />&nbsp;Property file: {{key}}
			<br><br>
			<textarea name="contents" rows="10" cols="450" class="well" style="width: 95%;">{{value}}</textarea>
			<br>
			</form>
		</td>
	</tr>
</table>