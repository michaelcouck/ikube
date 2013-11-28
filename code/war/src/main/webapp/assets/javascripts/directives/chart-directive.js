module.directive('chart', function($http) {
	return {
		restrict : 'E',
		scope : true,
		link : function($scope, $elm, $attr) {
			var chart = null;
			var options = $scope[$attr.ngOptions];
			var data = $scope[$attr.ngModel];

			// var data = google.visualization.arrayToDataTable(data);
			// var indexingChart = new google.visualization.LineChart($elm[0]);
			// indexingChart.draw(data, $scope.options);

			$scope.$watch('data', function(v) {
				if (!chart) {
					chart = $.plot($elm, v, options);
					$elm.show();
				} else {
					chart.setData(v);
					chart.setupGrid();
					chart.draw();
				}
			});
		}
	}
});