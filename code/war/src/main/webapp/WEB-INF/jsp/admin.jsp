<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table ng-controller="ServersController" class="table table-condensed">
	<tr>
		<td>
			<div ng-show="server.cpuThrottling">
				<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Cpu Throttling:</b> {{server.cpuThrottling}}<br>
			</div>
			<div ng-show="!server.cpuThrottling">
				<img src="<c:url value="/images/icons/red_square.gif" />">&nbsp;<b>Cpu Throttling:</b> {{server.cpuThrottling}}<br>
			</div>
		</td>
		<td>
			&nbsp;&nbsp;&nbsp;<input ng-click="toggleCpuThrottling();" type="image" src="<c:url value="/images/icons/run_on_server.gif" />" value="Go!">
		</td>
		<td>Turns the cpu throttling on or off, potentially slowing down the indexing, for the whole cluster</td>
	</tr>
	<tr>
		<td>
			<div ng-show="server.threadsRunning">
				<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Threads running:</b> {{server.threadsRunning}}<br>
			</div>
			<div ng-show="!server.threadsRunning">
				<img src="<c:url value="/images/icons/red_square.gif" />">&nbsp;<b>Threads running:</b> {{server.threadsRunning}}<br>
			</div>
		</td>
		<td>
			<div ng-show="server.threadsRunning">
				&nbsp;&nbsp;&nbsp;<input ng-click="terminateAll();" type="image" src="<c:url value="/images/icons/run_on_server.gif" />" value="Go!">
			</div>
			<div ng-show="!server.threadsRunning">
				&nbsp;&nbsp;&nbsp;<input ng-click="startupAll();" type="image" src="<c:url value="/images/icons/run_on_server.gif" />" value="Go!">
			</div>
		</td>
		<td>Starts/stops all the thread pools if not initialized and refreshes the thread pools if already initialized, for the whole cluster</td>
	</tr>
</table>
<br><br>

<table ng-controller="ServersController">
	<tr>
		<th><img src="<c:url value="/images/icons/server.gif" />">&nbsp;Server log tails</th>
	</tr>
	<tr ng-repeat="server in servers">
		<td class="bordered" nowrap="nowrap" valign="bottom">
			<a ng-click="server.show=!server.show" href="#">
				<img src="<c:url value="/images/icons/web.gif" />">&nbsp;<b>Address</b> : {{server.address}} <br>
			</a>
			<div ng-show="!server.show">
				<textarea rows="10" cols="100">{{server.logTail}}</textarea>
			</div>
		</td>
	</tr>
</table>