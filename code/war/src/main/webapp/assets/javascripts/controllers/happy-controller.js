/**
 * @author Michael Couck
 * @since 13-01-2014
 */
module.controller('HappyController', function($scope, $http, $injector, $timeout, $log) {
	
	$scope.analyzing = false;
	$scope.map = undefined;

	$scope.status = 200;

	$scope.search = {
		fragment : true,
		firstResult : 0,
		maxResults : 0,
		indexName : 'twitter'
	};
	
	$scope.searchUrl = '/ikube/service/twitter/happy';
	
	$scope.doHappyTwitterSearch = function() {
		if ($scope.analyzing) {
			return;
		}
		$scope.analyzing = true;
		$timeout(function() {
			var url = getServiceUrl($scope.searchUrl);
			var promise = $http.post(url, $scope.search);
			promise.success(function(data, status) {
				$scope.analyzing = false;
				$scope.status = status;
			});
			promise.error(function(data, status) {
				$scope.analyzing = false;
				$scope.status = status;
				$log.log('Error in doHappyTwitterSearch : ' + status);
			});
		}, 1000);
	};
	
    /**
     * This function draws the map
     */
    $scope.drawGeoChart = function drawChart() {
        var options = {};
        var array = [
            [ 'Country', 'Popularity' ],
            [ 'Germany', 200 ],
            [ 'United States', 300 ],
            [ 'Brazil', 400 ],
            [ 'Canada', 500 ],
            [ 'France', 600 ],
            [ 'RU', 700 ],
            [ 'Belgium', -1000 ] ];
        var data = google.visualization.arrayToDataTable(array);
		var chart = new google.visualization.GeoChart(document.getElementById('geo_chart_div'));
		chart.draw(data, options);
	};
	
	// This function will put the markers on the map
	$scope.doMarkers = function() {
		// This resets the markers, i.e. removes them
		$scope.doShowMap($scope.showMap);
		return $timeout(function() {
			if (!!$scope.search.searchResults) {
				$log.log('Showing markers : ');
				angular.forEach($scope.search.searchResults, function(key, value) {
					var latitude = key['latitude'];
					var longitude = key['longitude'];
					var fragment = key['fragment'];
					var distance = key['distance'];
					if (!!latitude && !!longitude) {
						$log.log('       marker : ' + latitude + '-' + longitude);
						new google.maps.Marker({
							map : $scope.map,
							icon: getServiceUrl('/ikube/assets/images/icons/person_obj.gif'),
							position : new google.maps.LatLng(latitude, longitude),
							title : 'Name : ' + fragment + ', distance : ' + distance
						});
					}
				});
			}
		}, 100);
	};

	google.setOnLoadCallback($scope.drawGeoChart);

});