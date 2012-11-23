<script type="text/javascript">

var config = { packages : [ "corechart" ] };
google.load("visualization", "1", config);
google.setOnLoadCallback(drawSearchChart);
google.setOnLoadCallback(drawIndexChart);

setInterval(function() {
	drawIndexChart();
	// drawSearchChart();
	// alert('Drawing : ');
}, 10000);
		
function drawIndexChart() {
	var data = google.visualization.arrayToDataTable([
		[ 'Time', '192.168.1.4', '192.168.1.6' ], 
		[ '7.23', 1000, 400 ],
		[ '7.24', 1170, 460 ], 
		[ '8.25', 660, 1120 ],
		[ '9.26', 1030, 540 ],
		[ '10.59', 1030, 540 ]
	]);

	var options = {
		title : 'Indexing performance',
		legend : { position : 'top', textStyle : { color : 'blue', fontSize : 12 }}
	};

	var indexingChartDiv = document.getElementById('indexingChart');
	var indexingChart = new google.visualization.LineChart(indexingChartDiv);
	indexingChart.draw(data, options);
}

function drawSearchChart() {
	var data = google.visualization.arrayToDataTable([
		[ 'Time', '192.168.1.4', '192.168.1.6' ], 
		[ '7.23', 1000, 400 ],
		[ '7.24', 1170, 460 ], 
		[ '7.25', 660, 1120 ],
		[ '7.26', 1030, 540 ] ]);

	var options = {
		title : 'Search performance',
		legend : { position : 'top', textStyle : { color : 'blue', fontSize : 12 }}
	};

	var searchingChartDiv = document.getElementById('searchingChart');
	var searchingChart = new google.visualization.LineChart(searchingChartDiv);
	searchingChart.draw(data, options);
}

</script>

<table ng-controller="MonitorController">
	<tr>
		<td valign="top">
			<!-- Servers data -->
			<table>
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
					<td><div id="searchingChart" style="width: 750px; height: 200px;"></div></td>
				</tr>
				<tr>
					<td><div id="indexingChart" style="width: 750px; height: 200px;"></div></td>
				</tr>
			</table>
		</td>
	</tr>
</table>