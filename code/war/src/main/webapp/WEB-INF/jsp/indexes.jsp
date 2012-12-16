<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table ng-controller="IndexContextsController" width="100%" style="border : 1px solid #aaaaaa;">
	<tr>
		<th><img src="<c:url value="/images/icons/index.gif" />"><a href="#" ng-click="sortIndexContexts('name')">Index</a></th>
		<th><img src="<c:url value="/images/icons/open.gif" />"><a href="#" ng-click="sortIndexContexts('open')">Open</a></th>
		<th><img src="<c:url value="/images/icons/link_obj.gif" />"><a href="#" ng-click="sortIndexContexts('numDocs')">Documents</a></th>
		<th><img src="<c:url value="/images/icons/repository.gif" />"><a href="#" ng-click="sortIndexContexts('indexSize')">Size</a></th>
		<th><img src="<c:url value="/images/icons/register_view.gif" />"><a href="#" ng-click="sortIndexContexts('maxAge')">Max age</a></th>
		<th><img src="<c:url value="/images/icons/refresh.gif" />"><a href="#" ng-click="sortIndexContexts('latestIndexTimestamp')">Index timestamp</a></th>
		<th><img src="<c:url value="/images/icons/jar_l_obj.gif" />"><a href="#" ng-click="sortIndexContexts('indexDirectoryPath')">Path</a></th>
		<th><img src="<c:url value="/images/icons/progress_task.gif" />"><a href="#" ng-click="sortIndexContexts('throttle')">Throttle</a></th>
		<th><img src="<c:url value="/images/icons/launch_run.gif" />">Function</th>
	</tr>
	<tr ng-repeat="indexContext in indexContexts" ng-class-odd="'odd'" ng-class-even="'even'">
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.name}}</td>
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.open}}</td>
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.numDocs / 1000000}}</td>
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.indexSize / 1000000}}</td>
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.maxAge}}</td>
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.latestIndexTimestamp}}</td>
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.indexDirectoryPath}}</td>
		<td ng-class="{ active : indexContext.indexing == true }">{{indexContext.throttle}}</td>
		<td ng-class="{ active : indexContext.indexing == true }" nowrap="nowrap">
			<a href="#" ng-click="startIndexing(indexContext.name);">Index</a>&nbsp;|&nbsp;
			<a href="#" ng-click="deleteIndex(indexContext.name);">Delete</a>
		</td>
	</tr>
</table>