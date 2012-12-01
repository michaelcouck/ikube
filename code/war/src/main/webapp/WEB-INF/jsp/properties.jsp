<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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

<table ng-controller="PropertiesController" width="100%">
	<tr>
		<th><img src="<c:url value="/images/icons/run_on_server.gif" />">&nbsp;File and properties</th>
	</tr>
	<tr ng-model="propertyFiles" ng-repeat="(key, value) in propertyFiles">
		<td>
			<form id="{{$index}}" name="{{$index}}" action="<c:url value="/service/monitor/set-properties" />" method="post">
			<table>
				<tr>
					<td valign="top">
						<input name="file" type="text" value="{{key}}">
					</td>
					<td>
						<textarea name="contents" rows="15" cols="120">{{value}}</textarea>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<input id="button-{{$index}}" name="button-{{$index}}" type="submit" value="Update">
					</td>
				</tr>
			</table>
			</form>
		</td>
	</tr>
</table>