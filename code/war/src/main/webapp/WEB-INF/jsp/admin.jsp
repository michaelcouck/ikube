<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table ng-controller="ServersController" style="border : 1px solid #aaaaaa;" width="100%">
	<tr>
		<th colspan="2"><img src="<c:url value="/images/icons/launch_run.gif" />">&nbsp;Actions</th>
	</tr>
	<tr class="odd">
		<td>
			&nbsp;&nbsp;&nbsp;<input ng-click="startupAll();" type="image" src="<c:url value="/images/icons/run_on_server.gif" />" value="Go!">
		</td>
		<td>
			Starts all the schedules if not initialized and refreshes the thread pool if already initialized, for the whole cluster
		</td>
	</tr>
	<tr class="even">
		<td>
			&nbsp;&nbsp;&nbsp;<input ng-click="terminateAll();" type="image" src="<c:url value="/images/icons/run_on_server.gif" />" value="Go!">
		</td>
		<td>
			Terminates all currently executing actions and destroys the thread pool for the schedules, no further actions will run, for the whole cluster
		</td>
	</tr>
</table>
<br><br>

<table ng-controller="ServersController" style="border : 1px solid #aaaaaa;" width="100%">
	<tr>
		<th><img src="<c:url value="/images/icons/server.gif" />">&nbsp;Server log tails</th>
	</tr>
	<tr ng-repeat="server in servers">
		<td class="bordered" nowrap="nowrap" valign="bottom">
			<a ng-click="server.show=!server.show" href="#">
				&nbsp;&nbsp;&nbsp;<img src="<c:url value="/images/icons/web.gif" />">&nbsp;<b>Address</b> : {{server.address}} <br>
			</a>
			<div ng-show="!server.show">
				&nbsp;&nbsp;&nbsp;<textarea rows="10" cols="100">{{server.logTail}}</textarea>
			</div>
		</td>
	</tr>
</table>