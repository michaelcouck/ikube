//The controller that does the search
module.controller('SearcherController', function($scope, $http, $timeout) {
	
	$scope.searchUrl = '/ikube/service/search/json';
	$scope.fieldsUrl = '/ikube/service/monitor/fields';
	$scope.indexesUrl = '/ikube/service/monitor/indexes';
	$scope.searchAllUrl = '/ikube/service/search/json/all';
	$scope.headers = { headers: { 'Content-Type' : 'application/json' } };
	
	$scope.search = null;
	$scope.statistics = null;
	$scope.pagination = null;
	$scope.indexes;
	$scope.indexName;
	
	$scope.instantSearchStrings = null;
	
	$scope.doSearchPreProcessing = function(search) {
		$scope.statistics = null;
		var search = angular.copy($scope.search);
		
		var searchStrings = new Array();
		var searchFields = new Array();
		var typeFields = new Array();
		
		for (var i = 0; i < search.searchStrings.length; i++) {
			var searchString = search.searchStrings[i];
			if (!!searchString) {
				searchStrings.push(searchString);
				searchFields.push(search.searchFields[i]);
				typeFields.push('string');
			}
		}
		
		search.searchStrings = searchStrings;
		search.searchFields = searchFields;
		search.typeFields = typeFields;
		
		return search;
	};
	
	// Go to the web service for the results
	$scope.doSearch = function() {
		$scope.url = getServiceUrl($scope.searchUrl);
		var search = $scope.doSearchPreProcessing($scope.search);
		
		var promise = $http.post($scope.url, search);
		promise.success(function(data, status) {
			$scope.status = status;
			$scope.search.searchResults = data.searchResults;
			$scope.doSearchPostProcessing($scope.search);
		});
		promise.error(function(data, status) {
			$scope.status = status;
			alert('Data : ' + data + ', status : ' + status);
		});
	};
	
	// This function will search every field in every index, expensive!
	$scope.doSearchAll = function() {
		$scope.statistics.total = 0;
		$scope.search.searchFields = [];
		$scope.search.searchResults = null;
		$scope.search.indexName = null;
		$scope.url = getServiceUrl($scope.searchAllUrl);
		
		var promise = $http.post($scope.url, $scope.search);
		promise.success(function(data, status) {
			$scope.status = status;
			$scope.search.searchResults = data.searchResults;
			$scope.doSearchPostProcessing($scope.search);
		});
		promise.error(function(data, status) {
			alert('Data : ' + data + ', status : ' + status);
		});
	};
	
	$scope.doPagedSearch = function(firstResult) {
		$scope.search.firstResult = firstResult;
		if (!!$scope.search.indexName) {
			$scope.doSearch();
		} else {
			$scope.doSearchAll();
		}
	};
	
	$scope.doSearchPostProcessing = function(search) {
		if (!!search.searchResults && search.searchResults.length > 0) {
			$scope.statistics = search.searchResults.pop();
			if (!!$scope.statistics.total && $scope.statistics.total > 0) {
				$scope.doPagination();
			}
			if (!!search.coordinate && !!search.coordinate.latitude && !!search.coordinate.longitude) {
				$scope.doMarkers();
			}
		}
	};
	
	// This function resets the search and nulls the statistics and pagination
	$scope.resetSearch = function() {
		$scope.search = { 
			searchStrings : [],
			sortFields : [],
			distance : 10,
			fragment : true,
			firstResult : 0,
			maxResults : 10
		};
		$scope.statistics = {};
		$scope.pagination = null;
	};
	$scope.resetSearch();
	
	// This is called by the sub controller(s), probably the type ahead 
	// controller when the user has selected a search string
	$scope.$on('doSearch', function(event, search) {
		$scope.search.searchStrings = search.searchStrings;
		$scope.search.firstResult = 0;
		$scope.doSearchAll();
	});
	
	// Watch the index name and get the search fields
	$scope.$watch('search.indexName', function() {
		$scope.waitForDigest = function() {
			return $timeout(function() {
				$scope.search.searchResults = null;
				$scope.search.firstResult = 0;
				if (!!$scope.search.indexName) {
					$scope.doFields($scope.search.indexName);
					$scope.$apply();
				}
			}, 100);
		};
		$scope.waitForDigest();
    }, true);
	
	// This function gets all the index names from the server
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
					$scope.search.searchStrings = new Array($scope.search.searchFields.length);
					$scope.search.typeFields = new Array($scope.search.searchFields.length);
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
		// We just started a search and got the first results
		var pages = $scope.statistics.total / $scope.search.maxResults;
		// Create one 'page' for each block of results
		for (var i = 0; i < pages && i < 10; i++) {
			var firstResult = i * $scope.search.maxResults;
			$scope.pagination[i] = { page : i, firstResult : firstResult };
		};
		// Find the 'to' result being displayed
		var modulo = $scope.statistics.total % $scope.search.maxResults;
		$scope.search.endResult = $scope.search.firstResult + modulo == $scope.statistics.total ? $scope.statistics.total : $scope.search.firstResult + parseInt($scope.search.maxResults, 10);
		return;
	}
	
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