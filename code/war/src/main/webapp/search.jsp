<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<% response.setHeader("Access-Control-Allow-Origin", "*"); %>

<!doctype html>
<html>
<head>
	<meta http-equiv="Expires" content="-1">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	
	<title><tiles:insertAttribute name="title" /></title>
	<link rel="shortcut icon" href="<c:url value="/images/icons/favicon.ico" />">
	
	<meta name="Keywords" content="Ikube, Enterprise Search, Web Site Search, Database Search, High Volume" />
	<meta name="Description" content="Ikube Enterprise Search." />
			
	<link rel="stylesheet" href="<c:url value="/style/style.css" />" />

	<script src="http://www.google-analytics.com/ga.js" type="text/javascript"></script>
	<script src="http://maps.google.com/maps/api/js?sensor=false" type="text/javascript"></script>
    <script src="https://www.google.com/jsapi" type="text/javascript"></script>
	
    <link rel="stylesheet" href="<c:url value="/style/codemirror.css" />" type="text/css" />
	
	<script src="<c:url value="/js/angular.min.js" />" type="text/javascript"></script>
	<script src="<c:url value="/js/codemirror.js" />" type="text/javascript"></script>
    <script src="<c:url value="/js/runmode.js" />" type="text/javascript"></script>
    
    <!-- Must be after Angular -->
    <script src="<c:url value="/js/ikube.js" />" type="text/javascript"></script>
</head>

<body>

<script type="text/javascript">

	// The global map
	var map = null;
	
	// Focus on the first field in the form
	angular.element(document).ready(function() {
		doFocus('allWords');
	});
	
	// The controller that populates the indexes drop down
	module.controller('IndexesController', function($http, $scope) {
		$scope.index = null;
		$scope.indexes = null;
		$scope.doIndexes = function() {
			$scope.url = getServiceUrl('/ikube/service/monitor/indexes');
			var promise = $http.get($scope.url);
			promise.success(function(data, status) {
				$scope.indexes = data;
				$scope.status = status;
			});
		}
		$scope.doIndexes();
	});

	// The controller that does the search
	module.controller('SearcherController', function($http, $scope) {
		
		// The model data that we bind to in the form
		$scope.allWords = 'hotel'; // Default is hotel
		$scope.exactPhrase = '';
		$scope.oneOrMore = '';
		$scope.noneOfTheseWords = '';
		$scope.latitude = '-33.9693580'; // Default is cape town
		$scope.longitude = '18.4622110'; // Default is cape town
		$scope.distance = '20'; // Distance from point of origin
		$scope.pageBlock = 10; // Only results per page
		$scope.geospatial = false;
		$scope.endResult = 0;

		$scope.statistics = {};
		$scope.pagination = []

		// This function concatenates the search strings for all the predicate
		// data into a semi colon separated string that can be used in the advanced
		// search
		$scope.doSearchStrings = function() {
			var searchStrings = [];
			searchStrings.push($scope.allWords);
			searchStrings.push(';');
			searchStrings.push($scope.exactPhrase);
			searchStrings.push(';');
			searchStrings.push($scope.oneOrMore);
			searchStrings.push(';');
			searchStrings.push($scope.noneOfTheseWords);
			return searchStrings.join('');
		};
		
		// The form parameters we send to the server
		$scope.searchParameters = { 
			indexName : 'geospatial', // The default is the geospatial index
			searchStrings : $scope.doSearchStrings(),
			fragment : true,
			firstResult : 0,
			maxResults : $scope.pageBlock
		};
		
		// The configuration for the request to the server for the results
		$scope.config = { params : $scope.searchParameters };
		
		// Go to the web service for the results
		$scope.doSearch = function() {
			if (!$scope.geospatial) {
				// Advanced search
				$scope.url = getServiceUrl('/ikube/service/search/json/multi/advanced/all');
				$scope.searchParameters['searchStrings'] = $scope.doSearchStrings();
				delete $scope.searchParameters['distance'];
				delete $scope.searchParameters['latitude'];
				delete $scope.searchParameters['longitude'];
			} else {
				// Geospatial search
				$scope.url = getServiceUrl('/ikube/service/search/json/multi/spatial/all');
				$scope.searchParameters['searchStrings'] = $scope.allWords;
				$scope.searchParameters['distance'] = $scope.distance;
				$scope.searchParameters['latitude'] = $scope.latitude;
				$scope.searchParameters['longitude'] = $scope.longitude;
			}
			var promise = $http.get($scope.url, $scope.config);
			promise.success(function(data, status) {
				// Pop the statistics Json off the array
				$scope.data = data;
				$scope.status = status;
				$scope.statistics = $scope.data.pop();
				$scope.doPagination($scope.data);
				$scope.doMarkers();
			});
			promise.error(function(data, status) {
				$scope.status = status;
			});
		};
		
		// Sets the first result based on the pagination page requested
		$scope.doFirstResult = function(firstResult) {
			$scope.searchParameters.firstResult = firstResult;
		}
		
		// Creates the Json pagination array for the next pages in the search
		$scope.doPagination = function(data) {
			var total = $scope.statistics.total;
			// Exception or no results
			if (total == null || total == 0) {
				$scope.pagination = [];
				$scope.searchParameters.firstResult = 0;
				$scope.endResult = 0;
				return;
			}
			// We just started a search and got the first results
			var pages = total / $scope.pageBlock;
			// Create one 'page' for each block of results
			for (var i = 0; i < pages && i < $scope.pageBlock; i++) {
				var firstResult = i * $scope.pageBlock;
				var active = firstResult == $scope.searchParameters.firstResult ? 'black' : 'blue';
				$scope.pagination[i] = { page : i, firstResult : firstResult, active : active };
			};
			// Find the 'to' result being displayed
			var modulo = total % $scope.pageBlock;
			$scope.endResult = $scope.searchParameters.firstResult + modulo == total ? total : $scope.searchParameters.firstResult + $scope.pageBlock;
		}
		
		// This function will put the markers on the map
		$scope.doMarkers = function() {
			if ($scope.geospatial) {
				var latitude = $scope.searchParameters.latitude;
				var longitude = $scope.searchParameters.longitude;
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
					icon: '/ikube/images/icons/center_pin.png'
				});
				for (var key in $scope.data) {
					var datum = $scope.data[key];
					if (datum.latitude != null && datum.longitude) {
						pointMarker = new google.maps.Marker({
							map : map,
							position: new google.maps.LatLng(datum.latitude, datum.longitude),
							title : 'Name : ' + datum.name + ', distance : ' + datum.distance
						});
					}
				}
				// And finally set the waypoints
				$scope.doWaypoints(origin);
			}
		}
		
		// This function will put the way points on the map
		$scope.doWaypoints = function(origin) {
			var waypoints = [];
			var destination = origin;
			var maxWaypoints = 8;
			for (var key in $scope.data) {
				var waypoint = new google.maps.LatLng($scope.data[key].latitude, $scope.data[key].longitude);
				waypoints.push({ location: waypoint });
				destination = waypoint;
				if (waypoints.length >= maxWaypoints) {
					break;
				}
			}
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
			directionsService.route(request, 
				function(response, status) {
					if (status == google.maps.DirectionsStatus.OK) {
						directionsDisplay.setDirections(response);
					} else {
						alert ('Failed to get directions from Googy, sorry : ' + status);
					}
				}
			);
		}
		
	});
	
	/** This directive will just init the map and put it on the page. */
	module.directive('googleMap', function() {
		return {
			restrict : 'A',
			compile : function($tElement, $tAttributes, $transclude) {
				return function($scope, $element, $attributes) {
					$scope.$watch($tAttributes.event, function(value) {
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
</script>

<table ng-app="ikube" ng-controller="SearcherController" width="100%">
	<tr>
		<td width="20%">Collection : </td>
		<td width="20%" nowrap="nowrap">
			<select ng-controller="IndexesController" ng-model="searchParameters.indexName">
   				<option ng-repeat="index in indexes" value="{{index}}">{{index}}</option>
			</select>
			Geospatial : <input type="checkbox" ng-model="geospatial" name="geospatial">
		</td>
		<td width="340px" rowspan="8">
			<div id="map_canvas" google-map style="height: 340px; width: 550px; border : 1px solid black;"></div>
		</td>
	</tr>
	
	<tr>
		<td>All of these words:</td>
		<td><input id="allWords" name="allWords" ng-model="allWords" value="allWords"></td>
	</tr>
	<tr>
		<td>This exact word or phrase:</td>
		<td><input ng-model="exactPhrase" value="exactPhrase"></td>
	</tr>
	<tr>
		<td>One or more of these words:</td>
		<td><input ng-model="oneOrMore" value="oneOrMore"></td>
	</tr>
	<tr>
		<td>None of these words:</td>
		<td><input ng-model="noneOfTheseWords" value="noneOfTheseWords"></td>
	</tr>
	<tr>
		<td>Latitude:</td>
		<td><input ng-model="latitude" placeholder="latitude"></td>
	</tr>
	<tr>
		<td>Longitude:</td>
		<td><input ng-model="longitude" placeholder="longitude"></td>
	</tr>
	<tr>
		<td>Distance:</td>
		<td><input ng-model="distance" placeholder="distance"></td>
	</tr>
	
	<tr>
		<td colspan="2">
			<input type="button" value="Advanced search" ng-click="doSearch();">
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<td colspan="3">
			Showing results '{{searchParameters.firstResult}} 
			to {{endResult}} 
			of {{statistics.total}}' 
			for search '{{searchParameters.searchStrings}}', 
			corrections : {{statistics.corrections}}, 
			duration : {{statistics.duration}}</td>
	</tr>
	
	<tr>
		<td colspan="3" nowrap="nowrap">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" ng-click="
					doFirstResult(page.firstResult);
					doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
	<tr><td colspan="3">&nbsp;</td></tr>
	
	<tr ng-repeat="datum in data">
		<td colspan="3">
			<b>Id</b> : {{datum.id}}<br> 
			<b>Score</b> : {{datum.score}}<br>
			<b>Fragment</b> : <span ng-bind-html-unsafe="datum.fragment"></span><br>
			<b>Latitude</b> : {{datum.latitude}}<br>
			<b>Longitude</b> : {{datum.longitude}}<br>
			<b>Distance</b> : {{datum.distance}}<br>
			<br>
		</td>
	</tr>
	
	<tr><td colspan="3">&nbsp;</td></tr>
	
	<tr>
		<td colspan="3" nowrap="nowrap">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" ng-click="
					doFirstResult(page.firstResult);
					doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
</table>

</body>
</html>