/** This directive will draw and update the searching performance graph. */
module.directive('searching', function($http) {
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
				vAxis: { title : 'Searches',  titleTextStyle: { color: '#dddddd' } },
				hAxis : { textStyle : { color: '#dddddd' } }
			};
			$scope.drawSearchingChart = function() {
				$scope.url = getServiceUrl('/ikube/service/monitor/searching');
				var promise = $http.get($scope.url);
				promise.success(function(data, status) {
					$scope.status = status;
					data = google.visualization.arrayToDataTable(data);
					var searchingChart = new google.visualization.LineChart($elm[0]);
					searchingChart.draw(data, $scope.options);
				});
				promise.error(function(data, status) {
					$scope.status = status;
				});
			}
			
			// Initially draw the chart from the server data
			$scope.drawSearchingChart();
			
			// And re-draw it every few seconds to give the live update feel
			setInterval(function() {
				$scope.drawSearchingChart();
			}, chartRefreshInterval);
		}
	}
});