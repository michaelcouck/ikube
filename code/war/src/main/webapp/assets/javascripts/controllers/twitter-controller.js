/**
 * @author Michael Couck
 * @since 14-12-2013
 */
module.controller('TwitterController', function($scope, $http, $injector, $timeout, $log, $controller) {
	
	$scope.analyzing = false;
	$scope.map;
	$scope.config;
	
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
	
	$scope.showMap = true;
	$scope.statistics = undefined;
	
	$scope.createdAtIndex = 0;
	$scope.classificationIndex = 1;
	$scope.contentsIndex = 2;
	$scope.languageIndex = 3;
	
	// sortFields : ['created-at']
	$scope.search = {
		fragment : true,
		firstResult : 0,
		maxResults : 10,
		distance : 20,
		startHour : -24,
		endHour : 0,
		coordinate : angular.copy($scope.coordinate),
		indexName : 'twitter',
		searchStrings : ['0-12345678900000', '', '', ''],
		searchFields : ['created-at', 'classification', 'contents', 'language-original'],
		typeFields : ['range', 'string', 'string', 'string'],
		sortFields : ['created-at'],
		sortDirection : ['true']
	};
	
	$scope.searchClone;
	
	$scope.searchUrl = '/ikube/service/twitter/analyze';
	
	$scope.doConfig = function(configName) {
		$scope.config = $injector.get('configService').getConfig(configName);
		$scope.$on($scope.config.emitHierarchyFunction, function(event, search) {
			$scope.setSearchStrings(search.searchStrings);
		});
	};
	
	$scope.setSearchStrings = function(searchStrings) {
		$scope.search.searchStrings[$scope.contentsIndex] = searchStrings[0];
	};
	
	$scope.doTwitterSearch = function(classification) {
		if ($scope.analyzing) {
			return;
		}
		$scope.analyzing = true;
		$scope.status = undefined;
		$timeout(function() {
			// Set the time range to search within
			var fromHour = $scope.setTimeInMillisPlusHours($scope.search.startHour);
			var endHour = $scope.setTimeInMillisPlusHours($scope.search.endHour);
			var timeRange = [];
			timeRange.push(fromHour);
			timeRange.push('-');
			timeRange.push(endHour);
			$scope.search.searchStrings[$scope.createdAtIndex] = timeRange.join('');
			$scope.search.searchStrings[$scope.classificationIndex] = classification;
			
			$scope.searchClone = angular.copy($scope.search);
			// Build the search strings
			$scope.setParameters($scope.search, $scope.searchClone);
			
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
					$scope.doPagination();
				}
			});
			promise.error(function(data, status) {
				$scope.analyzing = false;
				$scope.status = status;
				$log.log('Error in doTwitterSearch : ' + status);
			});
		}, 1000);
	};
	
	// Creates the Json pagination array for the next pages in the search
	$scope.doPagination = function() {
		$scope.pagination = [];
		// We just started a search and got the first results
		var pages = $scope.statistics.total / $scope.search.maxResults;
		// Create one 'page' for each block of results
		for ( var i = 0; i < pages && i < 10; i++) {
			var firstResult = i * $scope.search.maxResults;
			$scope.pagination[i] = {
				page : i,
				firstResult : firstResult
			};
		};
		// Find the 'to' result being displayed
		var modulo = $scope.statistics.total % $scope.search.maxResults;
		$scope.search.endResult = $scope.search.firstResult + modulo == $scope.statistics.total ? $scope.statistics.total
				: $scope.search.firstResult + parseInt($scope.search.maxResults, 10);
		return;
	}
	
	$scope.doPagedSearch = function(firstResult) {
		$scope.search.firstResult = firstResult;
		$scope.doTwitterSearch($scope.search.searchStrings[$scope.classificationIndex]);
	};
	
	$scope.setParameters = function(search, searchClone) {
		searchClone.searchStrings = new Array();
		searchClone.searchFields = new Array();
		searchClone.typeFields = new Array();
		searchClone.searchResults = undefined;
		
		angular.forEach(search.searchStrings, function(searchString, index) {
			if (!!searchString) {
				$log.log('Added : ' + search.searchStrings[index] + ', ' + search.searchFields[index]);
				searchClone.searchStrings.push(search.searchStrings[index]);
				searchClone.searchFields.push(search.searchFields[index]);
				searchClone.typeFields.push(search.typeFields[index]);
			}
		});
	};
	
	$scope.setTimeInMillisPlusHours = function(hours) {
		var now = new Date();
		now.setTime(now.getTime() + (hours * 60 * 60 * 1000));
		return now.getTime();
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
	
	$scope.drawChart = function drawChart() {
		var data = undefined;
		if (!!$scope.search.timeLineSentiment) {
			data = google.visualization.arrayToDataTable($scope.search.timeLineSentiment);
		} else {
			data = google.visualization.arrayToDataTable([ [ 'Hours of history', 'Positive', 'Negative' ], [ '-4', 1030, 540 ], [ '-3', 660, 1120 ], [ '-2', 1170, 460 ], [ '-1', 1000, 400 ] ]);
		}
		var options = { title : 'Twitter sentiment timeline', curveType : 'function', backgroundColor: { fill : 'transparent' } };
		var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
		chart.draw(data, options);
	};
	

	$scope.drawGeoChart = function drawChart() {
		var data = google.visualization.arrayToDataTable([ [ 'Country', 'Popularity' ], [ 'Germany', 200 ], [ 'United States', 300 ], [ 'Brazil', 400 ], [ 'Canada', 500 ], [ 'France', 600 ], [ 'RU', 700 ] ]);
		var options = {};
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
						pointMarker = new google.maps.Marker({
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
	
	google.setOnLoadCallback($scope.drawChart);
	google.setOnLoadCallback($scope.drawGeoChart);

});