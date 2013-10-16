//The controller that does the search
module.controller('SearcherController', function($scope, $http) {
	
	$scope.fields = [];
	$scope.statistics = null;
	$scope.pageBlock = 10;
	$scope.pagination = null;
	$scope.search = { maxResults : $scope.pageBlock };
	$scope.headers = { headers: { 'Content-Type' : 'application/json' } };
	$scope.predicate = '';
	$scope.reverse = false;
	
	$scope.isNumeric = function(string) {
		return !isNaN(string);
	};
	
	$scope.doSearchPreProcessing = function(search) {
		if (search == undefined || search.searchStrings == undefined || search.searchFields == undefined || search.typeFields == undefined) {
			return;
		};
		// Iterate through the search object fields convert:
		// 1) The latitude and longitude from fields to a co-ordinate object
		// 2) The type to numeric in if the search string is all numbers 
		// 3) The type to range if the search string is all numbers and there is a hyphen in the middle
		for (var i = 0; i < search.searchStrings.length; i++) {
			var searchField = search.searchFields[i];
			var searchString = search.searchStrings[i];
			if (searchField == 'latitude') {
				search.coordinate.latitude = searchString;
				// search.distance = 10; 
			} else if (searchField == 'longitude') {
				search.coordinate.longitude = searchString;
			} else if ($scope.isNumeric(searchString)) {
				if (searchString.indexOf('-') == -1) {
					// This is numeric field i.e. 123456
					search.typeFields.splice(i, 1, 'numeric');
				} else {
					// This is a range query, i.e. 123-456
					search.typeFields.splice(i, 1, 'range');
				}
			}
		}
	};
	
	// Go to the web service for the results
	$scope.doSearch = function() {
		$scope.doSearchPreProcessing($scope.search);
		$scope.search.maxResults = $scope.pageBlock;
		if ($scope.search != undefined && 
			$scope.search.searchFields != undefined && 
			$scope.search.searchFields.indexOf('latitude') > -1 && 
			$scope.search.searchFields.indexOf('longitude') > -1) {
			$scope.url = getServiceUrl('/ikube/service/search/json/multi/spatial/json');
		} else {
			$scope.url = getServiceUrl('/ikube/service/search/json/complex/sorted/json');
		}
		var promise = $http.post($scope.url, $scope.search);
		promise.success(function(data, status) {
			$scope.search = data;
			$scope.status = status;
			if ($scope.search.searchResults != undefined && $scope.search.searchResults.length > 0) {
				$scope.doPagination();
				$scope.doMarkers();
			}
		});
		promise.error(function(data, status) {
			alert('Data : ' + data + ', status : ' + status);
		});
	};
	// We execute this once to get the search object from the server
	$scope.doSearch();
	
	// Get the fields for the index
	$scope.doFields = function(indexName) {
		$scope.url = getServiceUrl('/ikube/service/monitor/fields');
		$scope.parameters = { indexName : indexName };
		$scope.config = { params : $scope.parameters };
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.fields = [];
			$scope.fields = data;
			$scope.status = status;
			$scope.searchFields = {};
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	
	// Creates the Json pagination array for the next pages in the search
	$scope.doPagination = function() {
		$scope.pagination = [];
		$scope.statistics = $scope.search.searchResults.pop();
		// Exception or no results
		if ($scope.statistics == undefined || $scope.statistics.total == undefined || $scope.statistics.total == null || $scope.statistics.total == 0) {
			$scope.endResult = 0;
			$scope.search.firstResult = 0;
			return;
		}
		// We just started a search and got the first results
		var pages = $scope.statistics.total / $scope.pageBlock;
		// Create one 'page' for each block of results
		for (var i = 0; i < pages && i < 10; i++) {
			var firstResult = i * $scope.pageBlock;
			$scope.pagination[i] = { page : i, firstResult : firstResult };
		};
		// Find the 'to' result being displayed
		var modulo = $scope.statistics.total % $scope.pageBlock;
		$scope.endResult = $scope.search.firstResult + modulo == $scope.statistics.total ? $scope.statistics.total : $scope.search.firstResult + parseInt($scope.pageBlock, 10);
	}
	
	// Set and remove fields from the search object and local scoped objects 
	$scope.setField = function(field, value) {
		$scope.search[field] = value;
	}
	$scope.removeField = function(field, $index) {
		$scope.search[field].splice($index, 1);
	};
	$scope.pushField = function(field, value) {
		$scope.search[field].push(value);
	};
	$scope.pushFieldScope = function(item, value) {
		$scope[item].push(value);
	};
	$scope.resetSearch = function() {
		$scope.search.searchStrings.splice(0, $scope.search.searchStrings.length);
		$scope.search.searchFields.splice(0, $scope.search.searchFields.length);
		$scope.search.typeFields.splice(0, $scope.search.typeFields.length);
	};
	
	// This function will put the markers on the map
	$scope.doMarkers = function() {
		var latitude = $scope.search.coordinate.latitude;
		var longitude = $scope.search.coordinate.longitude;
		var origin = new google.maps.LatLng(latitude, longitude);
		var mapElement = document.getElementById('map_canvas');
		var options = {
			zoom: 13,
			center: new google.maps.LatLng(latitude, longitude),
			mapTypeId: google.maps.MapTypeId.ROADMAP
		};
		map = new google.maps.Map(mapElement, options);
		// Add the point or origin marker
		var marker = new google.maps.Marker({
			map : map,
			position: origin,
			title : 'You are here :) => [' + latitude + ', ' + longitude + ']',
			icon: '/ikube/img/icons/center_pin.png'
		});
		angular.forEach($scope.search.searchResults, function(key, value) {
		    // alert('Key : ' + key['latitude'] + ', ' + key['longitude']);
		    if (key['latitude'] != undefined && key['longitude'] != undefined) {
				pointMarker = new google.maps.Marker({
					map : map,
					position: new google.maps.LatLng(key['latitude'], key['longitude']),
					title : 'Name : ' + key['fragment'] + ', distance : ' + key['distance']
				});
			}
		});
		// And finally set the waypoints
		$scope.doWaypoints(origin);
	};
	
	// This function will put the way points on the map
	$scope.doWaypoints = function(origin) {
		var waypoints = [];
		var destination = origin;
		var maxWaypoints = 8;
		
		angular.forEach($scope.search.searchResults, function(key, value) {
		    if (key['latitude'] != undefined && key['longitude'] != undefined) {
				if (waypoints.length < maxWaypoints) {
					var waypoint = new google.maps.LatLng(key['latitude'], key['longitude']);
   				    waypoints.push({ location: waypoint });
        			destination = waypoint;
				}
			}
		});
		
		var rendererOptions = { map: map };
		var directionsDisplay = new google.maps.DirectionsRenderer(rendererOptions);
		var request = {
				origin: origin,
				destination: destination,
				waypoints: waypoints,
				optimizeWaypoints : true,
				travelMode: google.maps.TravelMode.DRIVING,
				unitSystem: google.maps.UnitSystem.METRIC
		};
		var directionsService = new google.maps.DirectionsService();
		directionsService.route(request, function(result, status) {
            if (status == google.maps.DirectionsStatus.OK) {
              directionsDisplay.setDirections(result);
            }
        });
	};
	
});