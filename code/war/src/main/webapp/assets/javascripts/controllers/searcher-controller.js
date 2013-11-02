//The controller that does the search
module.controller('SearcherController', function($scope, $http, $timeout) {
	
	$scope.indexesUrl = '/ikube/service/monitor/indexes';
	$scope.geospatialUrl = '/ikube/service/search/json/geospatial';
	$scope.searchUrl = '/ikube/service/search/json';
	$scope.searchAllUrl = '/ikube/service/search/json/all';
	$scope.fieldsUrl = '/ikube/service/monitor/fields';
	
	$scope.statistics = null;
	$scope.pageBlock = 10;
	$scope.pagination = null;
	$scope.search = { 
		maxResults : $scope.pageBlock,
		fragment : true
	};
	$scope.headers = { headers: { 'Content-Type' : 'application/json' } };
	$scope.reverse = false;
	$scope.indexes;
	$scope.indexName;
	$scope.searching = false;
	
	// Watch the index name and get the search fields
	$scope.$watch('indexName', function() {
		$scope.waitForDigest = function() {
			return $timeout(function() {
				$scope.doFields($scope.indexName);
			}, 100);
		};
		$scope.waitForDigest();
    }, true);
	
	// This is called by the sub controller(s), probably the 
	// type ahead controller when the user has selected a 
	// search string
	$scope.$on('doSearch', function(event, search) {
		$scope.search.searchStrings = search.searchStrings;
		$scope.doSearchAll();
	});
	
	$scope.doIndexes = function() {
		var url = getServiceUrl($scope.indexesUrl);
		var promise = $http.get(url);
		promise.success(function(data, status) {
			$scope.indexes = data;
		});
		promise.error(function(data, status) {
			alert('Data : ' + data + ', status : ' + status);
		});
	}
	$scope.doIndexes();
	
	$scope.doSearchPreProcessing = function(search) {
		if (!!search && !!search.searchStrings && !!search.searchFields && !!search.typeFields) {
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
	};
	
	// Go to the web service for the results
	$scope.doSearch = function() {
		$scope.doSearchPreProcessing($scope.search);
		$scope.search.maxResults = $scope.pageBlock;
		if (!!$scope.search.searchFields && 
			!!$scope.search.searchFields.indexOf('latitude') && 
			!!$scope.search.searchFields.indexOf('longitude')) {
			$scope.url = getServiceUrl($scope.geospatialUrl);
		} else {
			$scope.url = getServiceUrl($scope.searchUrl);
		}
		var promise = $http.post($scope.url, $scope.search);
		promise.success(function(data, status) {
			$scope.search = data;
			$scope.status = status;
			if (!!$scope.search.searchResults && $scope.search.searchResults.length > 0) {
				$scope.doPagination();
				$scope.doMarkers();
			}
		});
		promise.error(function(data, status) {
			alert('Data : ' + data + ', status : ' + status);
		});
	};
	
	$scope.doSearchAll = function() {
		if (!$scope.searching) {
			$scope.searching = true;
			$scope.search.searchFields = null;
			$scope.search.maxResults = $scope.pageBlock;
			$scope.url = getServiceUrl($scope.searchAllUrl);
			var promise = $http.post($scope.url, $scope.search);
			promise.success(function(data, status) {
				$scope.searching = false;
				$scope.search = data;
				$scope.status = status;
				if (!!$scope.search.searchResults && $scope.search.searchResults.length > 0) {
					$scope.doPagination();
					$scope.doMarkers();
				}
			});
			promise.error(function(data, status) {
				$scope.searching = false;
				alert('Data : ' + data + ', status : ' + status);
			});
		}
	};
	
	// Get the fields for the index
	$scope.doFields = function(indexName) {
		$scope.url = getServiceUrl($scope.fieldsUrl);
		$scope.parameters = { indexName : indexName };
		$scope.config = { params : $scope.parameters };
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.search.searchFields = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
		
		var maxRetries = 5;
		$scope.wait = function() {
			return $timeout(function() {
				if (maxRetries-- > 0 && !!$scope.search.searchFields) {
					return $scope.search.searchFields;
				}
				return $scope.wait();
			}, 100);
		};
		return $scope.wait();
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
		alert('Field : ' + field + ', ' + value + ', ' + $scope.search[field]);
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
	/**
	 * Same as the above only the search object is populated, i.e. fields are set. 
	 */
	$scope.searchProperty = function(name, value, array) {
		if (!array) {
			$scope.search[name] = value;
		} else {
			$scope.search[name] = [value];
		}
		// alert('Search property : ' + $scope.search[name]);
	};
	
	// This function will put the markers on the map
	$scope.doMarkers = function() {
		if (!!$scope.search.coordinate && !!$scope.search.coordinate.latitude && !!$scope.search.coordinate.longitude) {
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
				var latitude = key['latitude'];
				var longitude = key['longitude'];
				var fragment = key['fragment'];
				var distance = key['distance'];
				// alert('Key : ' + key['latitude'] + ', ' + key['longitude']);
				if (!!latitude && !!longitude) {
					pointMarker = new google.maps.Marker({
						map : map,
						position: new google.maps.LatLng(latitude, longitude),
						title : 'Name : ' + fragment + ', distance : ' + distance
					});
				}
			});
			// And finally set the waypoints
			$scope.doWaypoints(origin);
		}
	};
	
	// This function will put the way points on the map
	$scope.doWaypoints = function(origin) {
		var waypoints = [];
		var destination = origin;
		var maxWaypoints = 8;
		
		angular.forEach($scope.search.searchResults, function(key, value) {
			var latitude = key['latitude'];
			var longitude = key['longitude'];
		    if (!!latitude && !!longitude) {
				if (waypoints.length < maxWaypoints) {
					var waypoint = new google.maps.LatLng(latitude, longitude);
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
	
	$scope.alert = function(name) {
		alert(name);
	};
	
	$scope.isNumeric = function(string) {
		return !isNaN(string);
	};
	
});