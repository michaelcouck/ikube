/**
 * @author Michael Couck
 * @since 14-12-2013
 */
module.controller('TwitterController', function($scope, $http, $injector, $timeout, $log) {
	
	$scope.analyzing = false;
	$scope.map = undefined;
	$scope.config = undefined;
	
	$scope.status = 200;
	// The running zoom in the map
	$scope.zoom = 7;
	$scope.languages = allLanguages;
	$scope.languages.sort();
	// Ths original co-ordinate, if it is exactly this then
	// the search will not take the co-ordinate into account
	$scope.coordinate = {
		latitude : -33.9,
		longitude : 18.4
	};
	
	$scope.showMap = false;
	$scope.showLanguages = false;
	$scope.statistics = undefined;
    $scope.languageIndex = 1;
	
	$scope.search = {
		fragment : true,
		firstResult : 0,
		maxResults : 0,
		distance : 20,
		startHour : -6,
        coordinate : angular.copy($scope.coordinate),
		indexName : 'twitter',
		searchStrings : ['', ''],
		searchFields : ['contents', 'language-original'],
		occurrenceFields : ['must', 'must'],
		typeFields : ['string', 'string']
	};
	
	$scope.searchClone = undefined;
	
	$scope.searchUrl = '/ikube/service/twitter/analyze';
	
	$scope.setSearchStrings = function(searchStrings) {
		$scope.search.searchStrings[0] = searchStrings[0];
	};
	
	$scope.doTwitterSearch = function() {
		if ($scope.analyzing) {
			return;
		}
		$scope.analyzing = true;
		$scope.status = undefined;
		$timeout(function() {
			$scope.searchClone = angular.copy($scope.search);
            $scope.searchClone.searchResults = null;

			// Remove the co-ordinate search field if it is not set
			if ($scope.searchClone.coordinate.latitude === $scope.coordinate.latitude) {
				$log.log('Removed coordinate');
				$scope.searchClone.coordinate = undefined;
			}
			
			var url = getServiceUrl($scope.searchUrl);
			var promise = $http.post(url, $scope.searchClone);
			promise.success(function(data, status) {
				$scope.analyzing = false;
				$scope.status = status;
				$scope.search.searchResults = data.searchResults;
				$scope.search.timeLineSentiment = data.timeLineSentiment;
				if (!!$scope.search.searchResults) {
					$scope.statistics = $scope.search.searchResults.pop();
					$scope.drawChart();
					$scope.doMarkers();
				}
			});
			promise.error(function(data, status) {
				$scope.analyzing = false;
				$scope.status = status;
				$log.log('Error in doTwitterSearch : ' + status);
			});
		}, 1000);
	};
	
	$scope.$watch('search.coordinate.latitude', function() {
		$scope.doShowMap($scope.showMap);
	});
	
	$scope.$watch('search.coordinate.latitude', function() {
		$scope.doShowMap($scope.showMap);
	});
	
	/**
	 * Displays the map
	 */
	$scope.doShowMap = function(showMap) {
		$scope.showMap = showMap;
		// We need to show the map div first before drawing it again
		$scope.doApply();

		// And wait for the apply to complete
		return $timeout(function() {
			// Re-draw the map on the page
			var latitude = $scope.search.coordinate.latitude;
			var longitude = $scope.search.coordinate.longitude;
			var origin = new google.maps.LatLng(latitude, longitude);
			var mapElement = document.getElementById('map_canvas');
			var options = {
					zoom : $scope.zoom,
					center : new google.maps.LatLng(latitude, longitude),
					mapTypeId : google.maps.MapTypeId.ROADMAP
			};
			$scope.map = new google.maps.Map(mapElement, options);
			// Add the point or origin marker
			var marker = new google.maps.Marker({
				map : $scope.map,
				position : origin,
				title : 'You are here => [' + latitude + ', ' + longitude + ']',
				icon : '/ikube/assets/images/icons/person_obj.gif'
			});
			google.maps.event.addListener($scope.map, 'click', function(event) {
				$scope.search.coordinate.latitude = event.latLng.lat();
				$scope.search.coordinate.longitude = event.latLng.lng();
				// Re-centre the map on the click event
				$scope.zoom = $scope.map.getZoom();
				$scope.doShowMap(showMap);
			});
		}, 100);
	};
	
	$scope.doClearCoordinate = function() {
		$scope.search.coordinate = angular.copy($scope.coordinate);
		$scope.doShowMap($scope.showMap);
	};
	
	$scope.doApply = function() {
		return $timeout(function() {
			$scope.$apply();
		}, 100);
	};

    /**
     * This function draws the time line chart with the positive and negative data
     */
	$scope.drawChart = function drawChart() {
		if (!$scope.search.timeLineSentiment) {
            return;
        }
        var data = google.visualization.arrayToDataTable($scope.search.timeLineSentiment);
        var options = { title : 'Twitter sentiment timeline', curveType : 'function', backgroundColor: { fill : 'transparent' } };
        var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
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

    $scope.doConfig = function(configName) {
        $scope.config = $injector.get('configService').getConfig(configName);
        $scope.$on($scope.config.emitHierarchyFunction, function(event, search) {
            $scope.setSearchStrings(search.searchStrings);
        });
    };
	
	google.setOnLoadCallback($scope.drawChart);

});