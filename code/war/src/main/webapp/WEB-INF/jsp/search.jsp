<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<% response.setHeader("Access-Control-Allow-Origin", "*"); %>

<script type="text/javascript">

	// The controller that does the search
	module.controller('SearcherController', function($http, $scope) {
		
		// The model data that we bind to in the form
		$scope.search = null;

		$scope.pageBlock = 10;
		$scope.statistics = {};
		$scope.pagination = []

		// Go to the web service for the results
		$scope.doSearch = function() {
			// Advanced search
			$scope.url = getServiceUrl('/ikube/service/search/json/complex/sorted/json');
			var promise = $http.get($scope.url, $scope.search);
			promise.success(function(data, status) {
				// Pop the statistics Json off the array
				$scope.search = data;
				$scope.status = status;
				$scope.statistics = $scope.search.searchResults.pop();
				$scope.doPagination($scope.search.searchResults);
			});
			promise.error(function(data, status) {
				$scope.status = status;
			});
		};
		// We execute this once to get the search object from the server
		$scope.doSearch();
		
		// Sets the first result based on the pagination page requested
		$scope.doFirstResult = function(firstResult) {
			$scope.search.firstResult = firstResult;
		}
		
		// Creates the Json pagination array for the next pages in the search
		$scope.doPagination = function(data) {
			$scope.pagination = [];
			var total = $scope.statistics.total;
			// Exception or no results
			if (total == null || total == 0) {
				$scope.search.firstResult = 0;
				$scope.endResult = 0;
				return;
			}
			// We just started a search and got the first results
			var pages = total / $scope.pageBlock;
			// Create one 'page' for each block of results
			for (var i = 0; i < pages && i < $scope.pageBlock; i++) {
				var firstResult = i * $scope.pageBlock;
				var active = firstResult == $scope.search.firstResult ? 'black' : 'blue';
				$scope.pagination[i] = { page : i, firstResult : firstResult, active : active };
			};
			// Find the 'to' result being displayed
			var modulo = total % $scope.pageBlock;
			$scope.endResult = $scope.search.firstResult + modulo == total ? total : $scope.search.firstResult + $scope.pageBlock;
		}
	});
	
</script>

<table ng-controller="SearcherController" class="table" style="margin-top: 55px;">
	<form ng-submit="doSearch()">
	<tr class="odd" nowrap="nowrap" valign="bottom">
		<td><b>Collection:</b></td>
		<td>
			<select ng-controller="IndexesController" ng-model="searchParameters.indexName">
				<option ng-repeat="index in indexes" value="{{index}}">{{index}}</option>
			</select>
		</td>
	</tr>
	<tr class="even" nowrap="nowrap" valign="bottom">
		<td><b>All of these words:</b></td>
		<td><input id="bla" name="bla" ng-model="must push the new one to the search object"></td>
	</tr>
	
	<tr class="even" nowrap="nowrap" valign="bottom">
		<td colspan="2">
			<button type="submit" class="btn" id="submit" name="submit">Go!</button>
		</td>
	</tr>
	</form>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr nowrap="nowrap" valign="bottom">
		<td colspan="2">
			Showing results '{{search.firstResult}} 
			to {{endResult}} 
			of {{statistics.total}}' 
			for search '{{search.searchStrings}}', 
			corrections : {{statistics.corrections}}, 
			duration : {{statistics.duration}}</td>
	</tr>
	
	<tr nowrap="nowrap" valign="bottom">
		<td colspan="2" nowrap="nowrap">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" ng-click="
					doFirstResult(page.firstResult);
					doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr ng-repeat="datum in data" ng-class-odd="'odd'" ng-class-even="'even'">
		<td colspan="2">
			<span ng-hide="!datum.id"><b>Identifier</b> : {{datum.id}}<br></span> 
			<b>Score</b> : {{datum.score}}<br>
			<b>Fragment</b> : <span ng-bind-html-unsafe="datum.fragment"></span><br>
			<br>
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<td colspan="2" nowrap="nowrap">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" ng-click="doFirstResult(page.firstResult);doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
</table>