<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table ng-controller="IndexContextsController">
	<tr>
		<th nowrap="nowrap">Name<button ng-click="sort('name')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Open<button ng-click="sort('open')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Documents<button ng-click="sort('numDocsForSearchers')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Size<button ng-click="sort('snapshot.indexSize')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Timestamp<button ng-click="sort('snapshot.latestIndexTimestamp')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Path<button ng-click="sort('indexDirectoryPath')"><i class="icon-sort"></i></button></th>
		<th nowrap="nowrap">Function</th>
	</tr>
	<!-- ng-class="{ 'well' : indexContext.indexing == true }" -->
	<tr ng-repeat="indexContext in indexContexts | orderBy:orderProp:direction">
		<td>{{indexContext.name}}</td>
		<td>{{indexContext.open}}</td>
		<td>{{indexContext.numDocsForSearchers / 1000000}}</td>
		<td>{{indexContext.snapshot.indexSize / 1000000}}</td>
		<!-- <td>{{indexContext.maxAge}}</td> -->
		<td>{{indexContext.snapshot.latestIndexTimestamp}}</td>
		<td nowrap="nowrap">{{indexContext.indexDirectoryPath}}</td>
		<!-- <td">{{indexContext.throttle}}</td> -->
		<td nowrap="nowrap">
			<div>
				<button ng-click="startIndexing(indexContext.name);" title="Starts the indexing for this collection">Start</button>
				<button ng-click="terminateIndexing(indexContext.name);" title="Stops the indexing for this collection">Stop</button>
				<button ng-click="deleteIndex(indexContext.name);" title="Deletes the index on the file system">Delete</button>
			</div>
			<div>
				<a href="<c:url value="/crud.html" />" ng-click="getIndexContext(indexContext.name);" title="Edit the collection properties and children">Edit collection</a>
			</div>
		</td>
	</tr>
</table>