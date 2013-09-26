<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table ng-controller="IndexContextsController" class="table" style="margin-top: 55px;">
	<tr>
		<th colspan="9"><img src="<c:url value="/images/icons/launch_run.gif" />">&nbsp;Local server indexes</th>
	</tr>
	<tr>
		<th><img src="<c:url value="/images/icons/index.gif" />">&nbsp;<a href="#" ng-click="sortIndexContexts('name')">Name</a></th>
		<th><img src="<c:url value="/images/icons/open.gif" />">&nbsp;<a href="#" ng-click="sortIndexContexts('open')">Open</a></th>
		<th><img src="<c:url value="/images/icons/link_obj.gif" />">&nbsp;<a href="#" ng-click="sortIndexContexts('numDocs')">Documents</a></th>
		<th><img src="<c:url value="/images/icons/repository.gif" />">&nbsp;<a href="#" ng-click="sortIndexContexts('indexSize')">Size</a></th>
		<%-- <th><img src="<c:url value="/images/icons/register_view.gif" />">&nbsp;<a href="#" ng-click="sortIndexContexts('maxAge')">Max age</a></th> --%>
		<th><img src="<c:url value="/images/icons/refresh.gif" />">&nbsp;<a href="#" ng-click="sortIndexContexts('latestIndexTimestamp')">Index timestamp</a></th>
		<th><img src="<c:url value="/images/icons/jar_l_obj.gif" />">&nbsp;<a href="#" ng-click="sortIndexContexts('indexDirectoryPath')">Path</a></th>
		<%-- <th><img src="<c:url value="/images/icons/progress_task.gif" />">&nbsp;<a href="#" ng-click="sortIndexContexts('throttle')">Throttle</a></th> --%>
		<%-- <th><img src="<c:url value="/images/icons/launch_run.gif" />">&nbsp;Function</th> --%>
	</tr>
	<tr ng-repeat="indexContext in indexContexts" ng-class-odd="'odd'" ng-class-even="'even'">
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.name}}</td>
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.open}}</td>
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.numDocsForSearchers / 1000000}}</td>
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.snapshot.indexSize / 1000000}}</td>
		<!-- <td ng-class="{ active : indexContext.indexing == true }">{{indexContext.maxAge}}</td> -->
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.snapshot.latestIndexTimestamp}}</td>
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.indexDirectoryPath}}</td>
		<!-- <td ng-class="{ active : indexContext.indexing == true }">{{indexContext.throttle}}</td> -->
		<!-- <td ng-class="{ active : indexContext.indexing == true }" nowrap="nowrap">
			<a href="#" ng-click="startIndexing(indexContext.name);">Index</a>&nbsp;|&nbsp;
			<a href="#" ng-click="terminateIndexing(indexContext.name);">Terminate</a>&nbsp;|&nbsp;
			<a href="#" ng-click="deleteIndex(indexContext.name);">Delete</a>
		</td> -->
	</tr>
</table>