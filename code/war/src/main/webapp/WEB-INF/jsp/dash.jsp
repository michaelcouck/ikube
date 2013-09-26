<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table ng-controller="ServersController">
	<tr>
		<th>Address</th>
		<th>CPU Load</th>
		<th>Free Mem</th>
		<th>Max Mem</th>
		<th>Tot Mem</th>
		<th>Disk space</th>
		<th>Running</th>
		<th>Throttling</th>
		<th>Age</th>
		<th>Processors</th>
		<th>Archi</th>
	</tr>
	<tr ng-repeat="server in servers" ng-class-odd="'odd'" ng-class-even="'even'">
		
		<td>
			<div ng-show="!cpuLoadTooHigh(server)">
				<i class="search">{{server.address}}</i><br>
				<span class="icon-hand">{{server.address}}</span><br>
				<span class="icon-pencil">{{server.address}}</span>
				<p><i class="icon-camera-retro icon-large"></i> icon-camera-retro</p>
			</div>
			<div ng-show="cpuLoadTooHigh(server)">
				<img src="<c:url value="/images/icons/web.gif" />"><font color="red">{{server.address}}</font><br>
			</div>
		</td>
		<td>{{server.averageCpuLoad}}</td>
		<td>{{server.freeMemory}}</td>
		<td>{{server.maxMemory}}</td>
		<td>{{server.totalMemory}}</td>
		<td>{{server.freeDiskSpace}}</td>
		<td>
			<div ng-show="server.threadsRunning">
				<img src="<c:url value="/images/icons/open.gif" />">{{server.threadsRunning}}<br>
			</div>
			<div ng-show="!server.threadsRunning">
				<img src="<c:url value="/images/icons/red_square.gif" />">{{server.threadsRunning}}<br>
			</div>
		</td>
		<td>
			<div ng-show="server.cpuThrottling">
				<img src="<c:url value="/images/icons/open.gif" />">{{server.cpuThrottling}}<br>
			</div>
			<div ng-show="!server.cpuThrottling">
				<img src="<c:url value="/images/icons/red_square.gif" />">{{server.cpuThrottling}}<br>
			</div>
		</td>
		<td>{{date(server.age)}}</td>
		<td>{{server.processors}}</td>
		<td>{{server.architecture}}</td>
	</tr>
</table>
<br><br>

<!-- <td valign="top" style="width: 80%;">
	<div searching>Searching performance graph</div>
	<div indexing>The indexing performance graph</div>
</td> -->

<table ng-controller="ActionsController" width="100%">
	<tr>
		<th><img src="<c:url value="/images/icons/server.gif" />">&nbsp;Server</th>
		<th><img src="<c:url value="/images/icons/jar_l_obj.gif" />">&nbsp;Action</th>
		<th><img src="<c:url value="/images/icons/index.gif" />">&nbsp;Index</th>
		<th><img src="<c:url value="/images/icons/run_on_server.gif" />">&nbsp;Indexable</th>
		<th><img src="<c:url value="/images/icons/link_obj.gif" />">&nbsp;Per minute</th>
		<th><img src="<c:url value="/images/icons/link_obj.gif" />">&nbsp;Total docs</th>
		<th><img src="<c:url value="/images/icons/launch_run.gif" />">&nbsp;Start time</th>
		<th><img src="<c:url value="/images/icons/launch_run.gif" />">&nbsp;End time</th>
	</tr>
	<tr ng-repeat="action in actions" ng-class-odd="'odd'" ng-class-even="'even'">
		<td ng-class="'bordered'">{{action.server.address}}</td>
		<td ng-class="'bordered'">{{action.id}}:{{action.actionName}}</td>
		<td ng-class="'bordered'">{{action.indexName}}</td>
		<td ng-class="'bordered'">{{action.indexableName}}</td>
		<td ng-class="'bordered'">{{action.snapshot.docsPerMinute}}</td>
		<td ng-class="'bordered'">{{action.snapshot.numDocsForIndexWriters}}</td>
		<td ng-class="'bordered'">{{action.startTime}}</td>
		<td ng-class="'bordered'">{{action.endTime}}</td>
	</tr>
</table>