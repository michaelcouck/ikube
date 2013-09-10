<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
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

<table ng-controller="DatabaseController" style="border : 1px solid #aaaaaa;" width="100%">
	<tr ng-model="indexes" ng-repeat="entity in indexes" ng-class-odd="'odd'" ng-class-even="'even'">
		<td class="bordered" valign="bottom">
			<form id="{{$index}}" name="{{$index}}" action="<c:url value="/service/monitor/set-properties" />" method="post">
				{{entity}}
			</form>
		</td>
	</tr>
</table>