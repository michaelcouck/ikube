<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table width="100%" style="border : 1px solid #aaaaaa;">
	<tr>
		<th colspan="2"><img src="<c:url value="/images/icons/launch_run.gif" />">&nbsp;Cluster dash board</th>
	</tr>
	<tr>
		<td ng-controller="ServersController" class="bordered" nowrap="nowrap" valign="top">
			<div ng-repeat="server in servers" ng-class-odd="'odd'" ng-class-even="'even'">
				<a ng-click="server.show=!server.show" href="#">
					<img src="<c:url value="/images/icons/web.gif" />">&nbsp; <font color="black"><b>Address:</b></font> {{server.address}}<br>
				</a>
				<img src="<c:url value="/images/icons/index_performance.gif" />">&nbsp;<b>Processors:</b> {{server.processors}}<br>
				
				<div ng-show="server.threadsRunning">
					<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Threads running:</b> {{server.threadsRunning}}<br>
				</div>
				<div ng-show="!server.threadsRunning">
					<img src="<c:url value="/images/icons/red_square.gif" />">&nbsp;<b>Threads running:</b> {{server.threadsRunning}}<br>
				</div>
				
				<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Average cpu load:</b> {{server.averageCpuLoad}}<br>
				<div ng-show="server.show">
					<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Free memory:</b> {{server.freeMemory}}<br>
					<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Max memory:</b> {{server.maxMemory}}<br>
					<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Total memory:</b> {{server.totalMemory}}<br>
					<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Free disk space:</b> {{server.freeDiskSpace}}<br>
					<div ng-show="server.cpuThrottling">
						<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Cpu Throttling:</b> {{server.cpuThrottling}}<br>
					</div>
					<div ng-show="!server.cpuThrottling">
						<img src="<c:url value="/images/icons/red_square.gif" />">&nbsp;<b>Cpu Throttling:</b> {{server.cpuThrottling}}<br>
					</div>
					<img src="<c:url value="/images/icons/server.gif" />">&nbsp;<b>Age:</b> {{date(server.age)}}<br>
					<img src="<c:url value="/images/icons/memory_view.gif" />">&nbsp;<b>Architecture:</b> {{server.architecture}}<br>
				</div>
			</div>
		</td>
		<td valign="top" style="width: 80%;">
			<div searching><!-- Searching performance graph --></div>
			<div indexing><!-- The indexing performance graph --></div>
		</td>
	</tr>
</table>
<br><br>

<table ng-controller="ActionsController" width="100%">
	<tr>
		<th colspan="7"><img src="<c:url value="/images/icons/launch_run.gif" />">&nbsp;Executing actions</th>
	</tr>
	<tr>
		<th><img src="<c:url value="/images/icons/server.gif" />">&nbsp;Server</th>
		<th><img src="<c:url value="/images/icons/jar_l_obj.gif" />">&nbsp;Action</th>
		<th><img src="<c:url value="/images/icons/index.gif" />">&nbsp;Index</th>
		<th><img src="<c:url value="/images/icons/run_on_server.gif" />">&nbsp;Indexable</th>
		<th><img src="<c:url value="/images/icons/link_obj.gif" />">&nbsp;Per minute</th>
		<th><img src="<c:url value="/images/icons/link_obj.gif" />">&nbsp;Total docs</th>
		<th><img src="<c:url value="/images/icons/launch_run.gif" />">&nbsp;Start time</th>
	</tr>
	<tr ng-repeat="action in actions" ng-class-odd="'odd'" ng-class-even="'even'">
		<td ng-class="'bordered'">{{action.server.address}}</td>
		<td ng-class="'bordered'">{{action.actionName}}</td>
		<td ng-class="'bordered'">{{action.indexName}}</td>
		<td ng-class="'bordered'">{{action.indexableName}}</td>
		<td ng-class="'bordered'">{{action.snapshot.docsPerMinute}}</td>
		<td ng-class="'bordered'">{{action.snapshot.totalDocsIndexed}}</td>
		<td ng-class="'bordered'">{{action.startTime}}</td>
	</tr>
</table>