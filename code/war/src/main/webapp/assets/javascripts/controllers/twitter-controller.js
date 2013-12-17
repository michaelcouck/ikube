/**
 * @author Michael Couck
 * @since 14-12-2013
 */
module.controller('TwitterController', function($scope, $http, $injector, $timeout, $log, $controller) {
	
	$scope.config;
	
	$scope.status = 200;
	// The running zoom in the map
	$scope.zoom = 13;
	$scope.languages = new Array();
	$scope.languages.push('', 'Chinese', 'Dutch', 'English', 'Spanish', 'Japanese', 'French', 'German', 'Swedish', 'Thai', 'Arabic', 'Turkish', 'Russian');
	$scope.languages.sort();
	$scope.language = undefined;
	// Ths original co-ordinate, if it is exactly this then
	// the search will not take the co-ordinate into account
	$scope.coordinate = {
		latitude : -33.9,
		longitude : 18.4
	};
	
	$scope.showMap = false;
	$scope.showResults = true;
	$scope.statistics = undefined;
	
	// 'contents', 'classification', 'language'
	// Re-define the search for this controller
	$scope.search = {
		fragment : true,
		firstResult : 0,
		maxResults : 10,
		distance : 20,
		startHour : -168,
		endHour : 0,
		coordinate : angular.copy($scope.coordinate),
		indexName : 'twitter',
		searchStrings : ['0-12345678900000'],
		searchFields : ['created-at'],
		typeFields : ['range']
	};
	$scope.searchUrl = '/ikube/service/twitter/analyze';
	
	$scope.doConfig = function(configName) {
		$scope.config = $injector.get('configService').getConfig(configName);
		$scope.$on($scope.config.emitHierarchyFunction, function(event, search) {
			$scope.setSearchStrings(search.searchStrings);
		});
	};
	
	$scope.setSearchStrings = function(searchStrings) {
		$scope.search.searchStrings[0] = searchStrings[0];
	};
	
	//searchStrings 
	$scope.doTwitterSearch = function(sentiment) {
		$scope.status = undefined;
		$timeout(function() {
			// Set the time range to search within
			var fromHour = $scope.setTimeInMillisPlusHours($scope.search.startHour);
			var endHour = $scope.setTimeInMillisPlusHours($scope.search.endHour);
			var timeRange = [];
			timeRange.push(fromHour);
			timeRange.push('-');
			timeRange.push(endHour);
			$scope.search.searchStrings[1] = timeRange.join('');
			
			var search = angular.copy($scope.search);
			
			// Set the sentiment if defined
			if (!!sentiment) {
				setSearchString(search, searchString, 'classification', 'string');
			}
			// Remove the co-ordinate search field if it is not set
			if (search.coordinate.latitude === $scope.coordinate.latitude) {
				$log.log('Removed coordinate');
				search.coordinate = undefined;
			}
			
			var url = getServiceUrl($scope.searchUrl);
			var promise = $http.post(url, search);
			promise.success(function(data, status) {
				$scope.status = status;
				$scope.search.searchResults = data.searchResults;
				if (!!$scope.search.searchResults) {
					$scope.statistics = $scope.search.searchResults.pop();
				}
			});
			promise.error(function(data, status) {
				$scope.status = status;
				$log.log('Error in doTwitterSearch : ' + status);
			});
		}, 1000);
	};
	
	$scope.setSearchString = function(search, searchString, searchField, typeField, index) {
		search.searchStrings.splice(searchString);
		search.searchFields.push(searchField);
		search.typeFields.push(typeField);
	};
	
	$scope.setTimeInMillisPlusHours = function(hours) {
		var now = new Date();
		now.setTime(now.getTime() + (hours * 60 * 60 * 1000));
		return now.getTime();
	};
	
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
			var map = new google.maps.Map(mapElement, options);
			// Add the point or origin marker
			var marker = new google.maps.Marker({
				map : map,
				position : origin,
				title : 'You are here => [' + latitude + ', ' + longitude + ']',
				icon : '/ikube/assets/images/icons/person_obj.gif'
			});
			google.maps.event.addListener(map, 'click', function(event) {
				$scope.search.coordinate.latitude = event.latLng.lat();
				$scope.search.coordinate.longitude = event.latLng.lng();
				// Re-centre the map on the click event
				$scope.zoom = map.getZoom();
				$scope.doShowMap(showMap);
			});
		}, 250);
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
		var data = google.visualization.arrayToDataTable([ [ 'Hours of history', 'Positive', 'Negative' ], [ '-4', 1030, 540 ], [ '-3', 660, 1120 ], [ '-2', 1170, 460 ], [ '-1', 1000, 400 ] ]);
		var options = { title : 'Twitter sentiment timeline', curveType : 'function' };
		var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
		chart.draw(data, options);
	};
	google.setOnLoadCallback($scope.drawChart);

});