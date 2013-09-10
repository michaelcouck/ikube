<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table ng-controller="DatabaseController" style="border : 1px solid #aaaaaa;" width="100%">
	<tr ng-model="entities" ng-repeat="entity in entities">
		<td class="bordered" valign="bottom">{{entity}}</td>
	</tr>
</table>