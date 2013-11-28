/** This directive will draw and update the indexing performance graph. */
module.directive('indexing', function($http) {
	return {
		restrict : 'A',
		scope : true,
		link : function($scope, $elm, $attr) {
			$scope.options = { 
				titleTextStyle : { color: '#bbbbbb' },
				height : 200,
				width : '100%',
				backgroundColor: { fill : 'transparent' },
				legend : { position : 'top', textStyle : { color : '#bbbbbb', fontSize : 12 } },
				vAxis: { title : 'Indexing',  titleTextStyle: { color: '#dddddd' } },
				hAxis : { textStyle : { color: '#dddddd' } }
			};
			$scope.drawIndexingChart = function() {
				$scope.url = getServiceUrl('/ikube/service/monitor/indexing');
				var promise = $http.get($scope.url);
				promise.success(function(data, status) {
					$scope.status = status;
					var data = google.visualization.arrayToDataTable(data);
					var indexingChart = new google.visualization.LineChart($elm[0]);
					indexingChart.draw(data, $scope.options);
				});
				promise.error(function(data, status) {
					$scope.status = status;
				});
			}
			// Initially draw the chart from the server data
			$scope.drawIndexingChart();
			// And re-draw it every few seconds to give the live update feel
			setInterval(function() {
				$scope.drawIndexingChart();
			}, chartRefreshInterval);
		}
	}
});