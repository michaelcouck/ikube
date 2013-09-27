<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table ng-controller="ServersController" class="table" style="margin-top: 55px;">
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
	<tr ng-repeat="server in servers">
		<td>
			<div ng-show="!cpuLoadTooHigh(server)">
				<span class="icon-pencil">{{server.address}}</span>
			</div>
			<div ng-show="cpuLoadTooHigh(server)">
				<a href="#" ng-click="show = !show"><img src="<c:url value="/images/icons/web.gif" />"><font color="red">{{server.address}}</font></a><br>
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

<div ng-show="show">
	<div searching style="width: 95%;">Searching performance graph</div>
	<div indexing style="width: 95%;">The indexing performance graph</div>
</div>

<div ng-controller="ActionsController">
	<div ng-hide="!actions.length">
		<table ng-controller="ActionsController" class="table">
			<tr>
				<th>Server</th>
				<th>Action</th>
				<th>Index</th>
				<th>Indexable</th>
				<th>Per minute</th>
				<th>Total docs</th>
				<th>Start time</th>
				<th>End time</th>
			</tr>
			<tr ng-repeat="action in actions" ng-class-odd="'odd'" ng-class-even="'even'">
				<td>{{action.server.address}}</td>
				<td>{{action.id}}:{{action.actionName}}</td>
				<td>{{action.indexName}}</td>
				<td>{{action.indexableName}}</td>
				<td>{{action.snapshot.docsPerMinute}}</td>
				<td>{{action.snapshot.numDocsForIndexWriters}}</td>
				<td>{{action.startTime}}</td>
				<td>{{action.endTime}}</td>
			</tr>
		</table>
	</div>
</div>