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
				vAxis: { title : 'Searches',  titleTextStyle: { color: '#bbbbbb' } },
				hAxis : { textStyle : { color: '#bbbbbb' } }
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
			};
			
			// Initially draw the chart from the server data
			$scope.drawSearchingChart();
			
			// And re-draw it every few seconds to give the live update feel
			setInterval(function() {
				$scope.drawSearchingChart();
			}, chartRefreshInterval);
		}
	}
});

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
				vAxis: { title : 'Indexing',  titleTextStyle: { color: '#bbbbbb' } },
				hAxis : { textStyle : { color: '#bbbbbb' } }
			};
			$scope.drawIndexingChart = function() {
				$scope.url = getServiceUrl('/ikube/service/monitor/indexing');
				var promise = $http.get($scope.url);
				promise.success(function(data, status) {
					$scope.status = status;
					data = google.visualization.arrayToDataTable(data);
					var indexingChart = new google.visualization.LineChart($elm[0]);
					indexingChart.draw(data, $scope.options);
				});
				promise.error(function(data, status) {
					$scope.status = status;
				});
			};
			// Initially draw the chart from the server data
			$scope.drawIndexingChart();
			// And re-draw it every few seconds to give the live update feel
			setInterval(function() {
				$scope.drawIndexingChart();
			}, chartRefreshInterval);
		}
	}
});

/** This directive will just init the map and put it on the page. */
module.directive('googleMap', function() {
	return {
		restrict : 'A',
		compile : function($tElement, $tAttributes, $transclude) {
			return function($scope, $element, $attributes) {
				$scope.$watch($tAttributes.event, function(value) {
					// The initial coordinates are for Cape Town
					var latitude = -33.9693580;
					var longitude = 18.4622110;
					var mapElement = document.getElementById('map_canvas');
					var options = {
						zoom: 13,
						center: new google.maps.LatLng(latitude, longitude),
						mapTypeId: google.maps.MapTypeId.ROADMAP
					};
					map = new google.maps.Map(mapElement, options);
				});
			}
		},
		controller : function($scope, $element, $location) {
			// Put something here?
		}
	};
});

/**
 * Load the Google visual after the directives to avoid some kind of recursive lookup.
 */
try {
	google.load('visualization', '1', { packages : [ 'corechart' ] });
} catch (err) {
	window.status = err;
}