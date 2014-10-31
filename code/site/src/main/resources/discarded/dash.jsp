<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table ng-controller="ServersController" class="table table-condensed">
	<tr>
		<th nowrap="nowrap">Address<button class="btn btn-mini btn-link" ng-click="sort('address')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">CPU<button class="btn btn-mini btn-link" ng-click="sort('averageCpuLoad')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Free Mem<button class="btn btn-mini btn-link" ng-click="sort('freeMemory')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Max Mem<button class="btn btn-mini btn-link" ng-click="sort('maxMemory')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Tot Mem<button class="btn btn-mini btn-link" ng-click="sort('totalMemory')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Disk<button class="btn btn-mini btn-link" ng-click="sort('freeDiskSpace')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Running<button class="btn btn-mini btn-link" ng-click="sort('threadsRunning')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Throttling<button class="btn btn-mini btn-link" ng-click="sort('cpuThrottling')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Age<button class="btn btn-mini btn-link" ng-click="sort('age')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Processors<button class="btn btn-mini btn-link" ng-click="sort('processors')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Archi<button class="btn btn-mini btn-link" ng-click="sort('architecture')"><i class="icon-sort"></i></button></th>
	</tr>
	<tr ng-repeat="server in servers">
		<td nowrap="nowrap"><span class="icon-pencil">{{server.address}}</span></td>
		<td>{{server.averageCpuLoad}}</td>
		<td>{{server.freeMemory}}</td>
		<td>{{server.maxMemory}}</td>
		<td>{{server.totalMemory}}</td>
		<td>{{server.freeDiskSpace}}</td>
		<td>
			<div ng-show="server.threadsRunning">
				<img src="<c:url value="/img/icons/open.gif" />">{{server.threadsRunning}}<br>
			</div>
			<div ng-show="!server.threadsRunning">
				<img src="<c:url value="/img/icons/red_square.gif" />">{{server.threadsRunning}}<br>
			</div>
		</td>
		<td>
			<div ng-show="server.cpuThrottling">
				<img src="<c:url value="/img/icons/open.gif" />">{{server.cpuThrottling}}<br>
			</div>
			<div ng-show="!server.cpuThrottling">
				<img src="<c:url value="/img/icons/red_square.gif" />">{{server.cpuThrottling}}<br>
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

<table ng-controller="ActionsController" class="table" ng-hide="!actions.length">
	<tr>
		<th>Server<button class="btn btn-mini btn-link" ng-click="sort('server.address')"><i class="icon-sort"></i></button></th>
		<th>Action<button class="btn btn-mini btn-link" ng-click="sort('actionName')"><i class="icon-sort"></i></button></th>
		<th>Index<button class="btn btn-mini btn-link" ng-click="sort('indexName')"><i class="icon-sort"></i></button></th>
		<th>Indexable<button class="btn btn-mini btn-link" ng-click="sort('indexableName')"><i class="icon-sort"></i></button></th>
		<th>Per minute<button class="btn btn-mini btn-link" ng-click="sort('snapshot.docsPerMinute')"><i class="icon-sort"></i></button></th>
		<th>Total docs<button class="btn btn-mini btn-link" ng-click="sort('snapshot.numDocsForIndexWriters')"><i class="icon-sort"></i></button></th>
		<th>Start time</th>
	</tr>
	<tr ng-repeat="action in actions">
		<td nowrap="nowrap">{{action.server.address}}</td>
		<td>{{action.id}}:{{action.actionName}}</td>
		<td>{{action.indexName}}</td>
		<td>{{action.indexableName}}</td>
		<td>{{action.snapshot.docsPerMinute}}</td>
		<td>{{action.snapshot.numDocsForIndexWriters}}</td>
		<td>{{action.startTime}}</td>
	</tr>
</table>