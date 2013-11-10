<div class="user-info">Path : {{indexContext.indexDirectoryPath}}/{{indexContext.name}}</div>
<div class="user-info">Backup path : {{indexContext.indexDirectoryPathBackup}}/{{indexContext.name}}</div>
<div class="user-content">
	Open : {{indexContext.open}}<br>
	Indexing : {{indexContext.indexing}}<br><br>
	
	Documents : {{indexContext.numDocsForSearchers}}<br>
	Timestamp : {{indexContext.snapshots.pop().latestIndexTimestamp}}<br>
	Index size on disk : {{indexContext.snapshot.indexSize}}<br><br>
	
	Max age : {{indexContext.maxAge}}<br>
	Throttle : {{indexContext.throttle}}<br>
	Merge factor : {{indexContext.mergeFactor}}<br>
	Buffered docs : {{indexContext.bufferedDocs}}<br>
	Buffer size : {{indexContext.bufferSize}}<br>
	Max field length : {{indexContext.maxFieldLength}}<br>
	Compound file : {{indexContext.compoundFile}}<br>
	Batch size : {{indexContext.batchSize}}<br>
	Internet batch size : {{indexContext.internetBatchSize}}<br>
	Max read length : {{indexContext.maxReadLength}}<br>
	Available disk space(if disk full running) : {{indexContext.availableDiskSpace}}<br>
	Delta index : {{indexContext.delta}}<br>
</div>
<div class="btn-group">
	<button class="button black mini" ng-click="startIndexing(indexContext.name);"><i class="icon-pencil"></i>Start</button>
	<button class="button black mini" ng-click="deleteIndex(indexContext.name);"><i class="icon-remove"></i>Delete</button>
	<button class="button black mini" onClick="enterpriseNotification();"><i class="icon-ok"></i>Edit</button>
	<button class="button black mini" ng-click="cancel();"><i class="icon-stop"></i>Cancel</button>
</div>