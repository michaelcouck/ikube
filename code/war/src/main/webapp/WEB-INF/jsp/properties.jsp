<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<script type="text/javascript">
$(document).ready(function() {
	var forms = document.getElementsByTagName('form');
	// We have to set the timeout because I think that there are
	// too many threads all trying to nob the dom, and well this one
	// just gets pushed out of the way :()
	setTimeout(function() {
		for ( var i = 0; i < forms.length; i++) {
			$('#' + forms[i].id).ajaxForm(function() {
				// Note that there is no logic in here, the jQuery form
				// add on just takes care of all the dirty details, and cancels
				// the browsers natural submit action, leaving the user high and 
				// dry on the page :)
				alert('Properties updated');
			});
		}
	}, 1000);
});
</script>

<table ng-controller="PropertiesController" style="border : 1px solid #aaaaaa;" width="100%">
	<tr>
		<th><img src="<c:url value="/images/icons/run_on_server.gif" />">&nbsp;System property files</th>
	</tr>
	<tr ng-model="propertyFiles" ng-repeat="(key, value) in propertyFiles" ng-class-odd="'odd'" ng-class-even="'even'">
		<td class="bordered" valign="bottom">
			<form id="{{$index}}" name="{{$index}}" action="<c:url value="/service/monitor/set-properties" />" method="post">
			<img src="<c:url value="/images/icons/jar_l_obj.gif" />" />&nbsp;Property file: {{key}}
			<br>
			<div ng-show="!value.show">
				<security:authorize access="hasRole('ROLE_ADMIN')">
					<textarea name="contents" rows="15" cols="120">{{value}}</textarea>
					<input id="button-{{$index}}" name="button-{{$index}}" type="submit" value="Update">
				</security:authorize>
			</div>
			<br>
			</form>
		</td>
	</tr>
</table>