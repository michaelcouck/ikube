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

	<!-- External styles and JavaScript -->
	<link rel="stylesheet" href="http://codemirror.net/lib/codemirror.css" type="text/css" />
	
	<script src="http://www.google-analytics.com/ga.js" type="text/javascript"></script>
	<script src="http://maps.google.com/maps/api/js?sensor=false" type="text/javascript"></script>
	<script src="http://code.jquery.com/jquery-1.8.2.js" type="text/javascript"></script>
	
	<script src="http://angular-ui.github.com/angular-ui/build/angular-ui.js" type="text/javascript"></script>
	<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min.js" type="text/javascript"></script>
	<script src="http://codemirror.net/lib/codemirror.js" type="text/javascript"></script>
    <script src="http://codemirror.net/lib/util/runmode.js" type="text/javascript"></script>
    
    <!-- Must be after Angular -->
    <script src="<c:url value="/js/ikube.js" />" type="text/javascript"></script>
</head>

<body>

<script type="text/javascript">
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
		$scope.pageBlock = 10; // Only results per page

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
			indexName : 'geospatial', // The default is the geospatial
			searchStrings : $scope.doSearchStrings(),
			fragment : true,
			firstResult : 0,
			endResult : 0,
			maxResults : $scope.pageBlock
		};
		
		// The configuration for the request to the server for the results
		$scope.config = { params : $scope.searchParameters };
		
		// Go to the web service for the results
		$scope.url = getServiceUrl('/ikube/service/search/json/multi/advanced/all');
		$scope.doSearch = function() {
			$scope.searchParameters['searchStrings'] = $scope.doSearchStrings();
			var promise = $http.get($scope.url, $scope.config);
			promise.success(function(data, status) {
				// Pop the statistics Json off the array
				$scope.statistics = data.pop();
				$scope.status = status;
				$scope.doPagination(data);
				$scope.data = data;
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
				$scope.searchParameters.endResult = 0;
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
			var endResult = $scope.searchParameters.firstResult + modulo == total ? total : $scope.searchParameters.firstResult + $scope.pageBlock;
			$scope.searchParameters.endResult = endResult;
		}
	});
</script>

<table ng-app="ikube" ng-controller="SearcherController">
	<tr>
		<td width="20%">Collection : </td>
		<td width="80%">
			<select ng-controller="IndexesController" ng-model="searchParameters.indexName">
   				<option ng-repeat="index in indexes" value="{{index}}">{{index}}</option>
			</select>
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
		<td colspan="2">
			<input type="button" value="Advanced search" ng-click="doSearch()">
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<td colspan="2">
			Showing results '{{searchParameters.firstResult}} 
			to {{searchParameters.endResult}} 
			of {{statistics.total}}' 
			for search '{{statistics.searchStrings}}', 
			corrections : {{statistics.corrections}}, 
			duration : {{statistics.duration}}</td>
	</tr>
	
	<tr>
		<td colspan="2" nowrap="nowrap">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" ng-click="
					doFirstResult(page.firstResult);
					doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr ng-repeat="datum in data">
		<td colspan="2">
			<b>Id</b> : {{datum.id}}<br> 
			<b>Score</b> : {{datum.score}}<br>
			<b>Fragment</b> : {{datum.fragment}}<br>
			<br>
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<td colspan="2" nowrap="nowrap">
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