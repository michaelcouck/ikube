<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style type="text/css">
.active {
   	background-color: #e2f6c0;
}
</style>

<table ng-controller="IndexContextsController" width="100%">
	<tr>
		<th><img src="<c:url value="/images/icons/index.gif" />">&nbsp;Index</th>
		<th><img src="<c:url value="/images/icons/open.gif" />">&nbsp;Open</th>
		<th><img src="<c:url value="/images/icons/link_obj.gif" />">&nbsp;Documents</th>
		<th><img src="<c:url value="/images/icons/repository.gif" />">&nbsp;Size</th>
		<th><img src="<c:url value="/images/icons/register_view.gif" />">&nbsp;Max age</th>
		<th><img src="<c:url value="/images/icons/refresh.gif" />">&nbsp;Index timestamp</th>
		<th><img src="<c:url value="/images/icons/jar_l_obj.gif" />">&nbsp;Path</th>
		<th><img src="<c:url value="/images/icons/progress_task.gif" />">&nbsp;Throttle</th>
		<th><img src="<c:url value="/images/icons/launch_run.gif" />">&nbsp;Function</th>
	</tr>
	<tr ng-repeat="indexContext in indexContexts" ng-class="{ active : indexContext.indexing == true }">
		<td>{{indexContext.name}}</td>
		<td>{{indexContext.open}}</td>
		<td>{{indexContext.numDocs}}</td>
		<td>{{indexContext.indexSize}}</td>
		<td>{{indexContext.maxAge}}</td>
		<td>{{indexContext.latestIndexTimestamp}}</td>
		<td>{{indexContext.indexDirectoryPath}}</td>
		<td>{{indexContext.throttle}}</td>
		<td>
			<a href="#" ng-click="startIndexing(indexContext.name);">Start indexing</a>
		</td>
	</tr>
</table>