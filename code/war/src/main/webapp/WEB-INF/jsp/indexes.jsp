<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table ng-controller="IndexContextsController" class="table table-condensed">
	<tr>
		<th nowrap="nowrap">Name<button class="btn btn-mini btn-link" ng-click="sort('name')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Open<button class="btn btn-mini btn-link" ng-click="sort('open')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Documents<button class="btn btn-mini btn-link" ng-click="sort('numDocsForSearchers')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Size<button class="btn btn-mini btn-link" ng-click="sort('snapshot.indexSize')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Timestamp<button class="btn btn-mini btn-link" ng-click="sort('snapshot.latestIndexTimestamp')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Path<button class="btn btn-mini btn-link" ng-click="sort('indexDirectoryPath')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Function</th>
	</tr>
	<tr ng-repeat="indexContext in indexContexts | orderBy:orderProp:direction" ng-class="{ 'well' : indexContext.indexing == true }">
		<td>{{indexContext.name}}</td>
		<td>{{indexContext.open}}</td>
		<td>{{indexContext.numDocsForSearchers / 1000000}}</td>
		<td>{{indexContext.snapshot.indexSize / 1000000}}</td>
		<!-- <td>{{indexContext.maxAge}}</td> -->
		<td>{{indexContext.snapshot.latestIndexTimestamp}}</td>
		<td nowrap="nowrap">{{indexContext.indexDirectoryPath}}</td>
		<!-- <td">{{indexContext.throttle}}</td> -->
		<td nowrap="nowrap">
			<div class="btn-group">
				<button class="btn btn-mini btn-success" ng-click="startIndexing(indexContext.name);" title="Starts the indexing for this collection">Start</button>
				<button class="btn btn-mini btn-warning" ng-click="terminateIndexing(indexContext.name);" title="Stops the indexing for this collection">Stop</button>
				<button class="btn btn-mini btn-danger" ng-click="deleteIndex(indexContext.name);" title="Deletes the index on the file system">Delete</button>
			</div>
			<div class="btn-group">
				<a href="<c:url value="/crud.html" />" class="btn btn-mini" ng-click="getIndexContext(indexContext.name);" title="Edit the collection properties and children">Edit collection</a>
			</div>
		</td>
	</tr>
</table>