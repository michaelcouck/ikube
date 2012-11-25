<table ng-controller="IndexContextsController" width="100%">
	<tr>
		<th>Index</th>
		<th>Open</th>
		<th>Documents</th>
		<th>Size</th>
		<th>Max age</th>
		<th>Index timestamp</th>
		<th>Path</th>
		<th>Function</th>
	</tr>
	<tr ng-repeat="indexContext in indexContexts">
		<td>{{indexContext.name}}</td>
		<td>{{indexContext.open}}</td>
		<td>{{indexContext.numDocs}}</td>
		<td>{{indexContext.indexSize}}</td>
		<td>{{indexContext.maxAge}}</td>
		<td>{{indexContext.latestIndexTimestamp}}</td>
		<td>{{indexContext.indexDirectoryPath}}</td>
		<td>
			<a href="#" ng-click="startIndexing(indexContext.name);">Start indexing</a>
		</td>
	</tr>
</table>