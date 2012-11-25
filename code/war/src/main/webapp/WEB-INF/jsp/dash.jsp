<table>
	<tr>
		<td valign="top">
			<!-- Servers data -->
			<table ng-controller="ServersController">
				<tr ng-repeat="server in servers">
					<td style="border : 1px solid #aaaaaa; padding : 5px;" nowrap="nowrap" valign="top">
						<b>Address</b> : {{server.address}} <br>
						<b>Age</b> : {{server.age}} <br>
						<b>Free memory</b> : {{server.freeMemory}} <br>
						<b>Max memory</b> : {{server.maxMemory}} <br>
						<b>Total memory</b> : {{server.totalMemory}} <br>
						<b>Free disk space</b> : {{server.freeDiskSpace}} <br>
						<b>Architecture</b> : {{server.architecture}} <br>
						<b>Processors</b> : {{server.processors}} <br>
						<b>Cpu load</b> : {{server.averageCpuLoad}} <br>
					</td>
				</tr>
				<tr><td>&nbsp;</td></tr>
			</table>
		</td>
		<td>
			<!-- Performance graphs -->
			<table>
				<tr>
					<td>
						<div searching style="width: 900px; height: 200px;"><!-- Searching performance graph --></div>
					</td>
				</tr>
				<tr>
					<td>
						<div indexing style="width: 900px; height: 200px;"><!-- The indexing performance graph --></div>
					</td>
				</tr>
				<tr>
					<td>
						<table ng-controller="ActionsController" width="100%">
							<tr>
								<th>Server</th>
								<th>Action</th>
								<th>Index</th>
								<th>Indexable</th>
								<th>Start time</th>
								<th>Duration</th>
								<th>Invocations</th>
								<th>Timestamp</th>
								<th>Functions</th>
							</tr>
							<tr ng-repeat="action in actions">
								<td>{{action.server}}</td>
								<td>{{action.actionName}}</td>
								<td>{{action.indexName}}</td>
								<td>{{action.indexableName}}</td>
								<td>{{action.startTime}}</td>
								<td>{{action.duration}}</td>
								<td>{{action.invocations}}</td>
								<td>{{action.timestamp}}</td>
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