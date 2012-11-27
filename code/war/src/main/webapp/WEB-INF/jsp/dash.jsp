<table width="100%">
	<tr>
		<td valign="top">
			<!-- Servers data -->
			<table ng-controller="ServersController">
				<tr ng-repeat="server in servers">
					<td style="border : 1px solid #aaaaaa; padding : 5px;" nowrap="nowrap" valign="top">
						<b>Age</b> : {{server.age}} <br>
						<b>Cpu load</b> : {{server.averageCpuLoad}} <br>
						<b>Processors</b> : {{server.processors}} <br>
						<b>Architecture</b> : {{server.architecture}} <br>
						<b>Address</b> : {{server.address}} <br>
						<b>Free memory</b> : {{server.freeMemory}} <br>
						<b>Max memory</b> : {{server.maxMemory}} <br>
						<b>Total memory</b> : {{server.totalMemory}} <br>
						<b>Free disk space</b> : {{server.freeDiskSpace}} <br>
					</td>
				</tr>
				<tr><td>&nbsp;</td></tr>
			</table>
		</td>
		<td width="100%">
			<!-- Performance graphs -->
			<table width="100%">
				<tr>
					<td width="100%">
						<div searching style="width: 95%; height: 200px;"><!-- Searching performance graph --></div>
					</td>
				</tr>
				<tr>
					<td width="100%">
						<div indexing style="width: 95%; height: 200px;"><!-- The indexing performance graph --></div>
					</td>
				</tr>
				<tr>
					<td width="100%" align="left">
						<table ng-controller="ActionsController" width="100%">
							<tr>
								<th width="7%">&nbsp;</th>
								<th>Server</th>
								<th>Action</th>
								<th>Index</th>
								<th>Indexable</th>
								<th>Docs per minute</th>
								<th>Start time</th>
								<th>Functions</th>
							</tr>
							<tr ng-repeat="action in actions">
								<td width="7%">&nbsp;</td>
								<td>{{action.server}}</td>
								<td>{{action.actionName}}</td>
								<td>{{action.indexName}}</td>
								<td>{{action.indexableName}}</td>
								<td>{{action.docsPerMinute}}</td>
								<td>{{action.startTime}}</td>
								<td>
									<a style="font-color : red;" href="#" ng-click="terminateIndexing(action.indexName);">Terrminate</a>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>