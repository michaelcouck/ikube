<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<% response.setHeader("Access-Control-Allow-Origin", "*"); %>

<div id="maincontent">

	<h2>Happy Planet</h2>
	

	<script type="text/javascript">

	// The controller that does the search
	module.controller('SentimentSearcherController', function($http, $scope) {
		
		// The model data that we bind to in the form
		$scope.contents = null;
		$scope.language = null;
		$scope.location = null;
		$scope.createdAt = new Date().getTime();

		$scope.statistics = {};
		$scope.pagination = [];
		
		$scope.typeFields = 'string;string;string;range';
		$scope.searchFields = 'contents;language;location;created-at';
		
		// This function concatenates the search strings for all the search predicate
		$scope.doSearchStrings = function() {
			var searchStrings = [];
			searchStrings.push($scope.contents);
			searchStrings.push(';');
			searchStrings.push($scope.language);
			searchStrings.push(';');
			searchStrings.push($scope.location);
			searchStrings.push(';');
			searchStrings.push($scope.createdAt);
			return searchStrings.join('');
		};

		// The form parameters we send to the server
		$scope.searchParameters = { 
			indexName : 'twitter',
			typeFields : $scope.typeFields,
			searchFields : $scope.searchFields,
			searchStrings : $scope.doSearchStrings(),
			fragment : true,
			firstResult : 0,
			maxResults : $scope.pageBlock
		};
		
		// The configuration for the request to the server for the results
		$scope.config = { params : $scope.searchParameters };
		
		// Go to the web service for the results
		$scope.doSearch = function() {
			// Numeric search against all the fields
			$scope.url = getServiceUrl('/ikube/service/search/json/complex');
			$scope.searchParameters['searchStrings'] = $scope.doSearchStrings();
			var promise = $http.get($scope.url, $scope.config);
			promise.success(function(data, status) {
				// Pop the statistics Json off the array
				$scope.data = data;
				$scope.status = status;
				$scope.statistics = $scope.data.pop();
				$scope.doPagination($scope.data);
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
			$scope.pagination = [];
			var total = $scope.statistics.total;
			// Exception or no results
			if (total == null || total == 0) {
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
		
		setInterval(function() {
			$scope.doSearch();
		}, 15000);
	});
	
	/** This directive will draw and update the searching performance graph. */
	module.directive('happy', function($http) {
		return {
			restrict : 'A',
			scope : true,
			link : function($scope, $elm, $attr) {
				$scope.options = { 
					title : 'Happy planet sentiment graph',
					height : 200,
					legend : { position : 'top', textStyle : { color : 'black', fontSize : 10 } } };
				$scope.drawSearchingChart = function() {
					$scope.url = getServiceUrl('/ikube/service/monitor/searching');
					var promise = $http.get($scope.url);
					promise.success(function(data, status) {
						$scope.status = status;
						var data = google.visualization.arrayToDataTable(data);
						var searchingChart = new google.visualization.LineChart($elm[0]);
						searchingChart.draw(data, $scope.options);
					});
					promise.error(function(data, status) {
						$scope.status = status;
					});
				}
				// Initially draw the chart from the server data
				$scope.drawSearchingChart();
				// And re-draw it every few seconds to give the live update feel
				setInterval(function() {
					$scope.drawSearchingChart();
				}, 15000);
			}
		}
	});
	
</script>

<br><br>
<table width="100%" style="border : 1px solid #aaaaaa;">
	<td valign="top" style="width: 80%;"><div happy><!-- The planet sentiment graph --></div></td>
	<td>And the latest tweets scrolling here</td>
</table>
<br><br>

<table ng-app="site" ng-controller="SentimentSearcherController" width="100%">
	<form ng-submit="doSearch()">
	<tr>
		<td>Key word:</td>
		<td><input id="contents" name="contents" ng-model="contents" value="contents"></td>
	</tr>
	<tr>
		<td>Language:</td>
		<td><input id="language" name="language" ng-model="language" value="language"></td>
	</tr>
	<tr>
		<td>Location/country:</td>
		<td><input id="location" name="location" ng-model="location" value="location"></td>
	</tr>
	<tr>
		<td>Created at range:</td>
		<td><input id="createdAt" name="createdAt" ng-model="createdAt" value="createdAt"></td>
	</tr>

	<tr>
		<td colspan="2">
			<button>Go!</button>
		</td>
	</tr>
	</form>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<td colspan="2">
			Showing results '{{searchParameters.firstResult}} 
			to {{endResult}} 
			of {{statistics.total}}' 
			for search '{{searchParameters.searchStrings}}', 
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
			<span ng-hide="!datum.id"><b>Identifier</b> : {{datum.id}}<br></span> 
			<b>Score</b> : {{datum.score}}<br>
			<span><b>Resource</b> : {{datum}}<br></span>
			<br>
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<td colspan="2" nowrap="nowrap">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" 
					ng-click="doFirstResult(page.firstResult);doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
</table>
	
</div>