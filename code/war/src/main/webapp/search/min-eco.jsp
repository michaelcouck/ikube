<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<% response.setHeader("Access-Control-Allow-Origin", "*"); %>

<script type="text/javascript">

	// The controller that does the search
	module.controller('MinEcoSearcherController', function($http, $scope) {
		
		// The model data that we bind to in the form
		$scope.NAME = 'Belgacom';
		$scope.PROVINCE = 'Flanders'
		$scope.CD_LGL_CO_TYP_FROM = '0'
		$scope.CD_LGL_CO_TYP_TO = '10';
		$scope.EMPLOYEES_COUNT_FROM = '100';
		$scope.EMPLOYEES_COUNT_TO = '10000';
		
		$scope.pageBlock = 10; // Only results per page
		$scope.endResult = 0;

		$scope.statistics = {};
		$scope.pagination = []

		// The form parameters we send to the server
		$scope.searchParameters = { 
			indexName : 'min-eco',
			searchStrings : $scope.NAME,
			fragment : true,
			firstResult : 0,
			maxResults : $scope.pageBlock
		};
		
		// The configuration for the request to the server for the results
		$scope.config = { params : $scope.searchParameters };
		
		// Go to the web service for the results
		$scope.doSearch = function() {
			// Numeric search against all the fields
			$scope.url = getServiceUrl('/ikube/service/search/json/numeric/all');
			$scope.searchParameters['searchStrings'] = $scope.numericSearchString;
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
	});
	
</script>

<table ng-app="ikube" ng-controller="MinEcoSearcherController" width="100%">
	<form ng-submit="doSearch()">
	<tr>
		<td>Collection : </td>
		<td>
			<select ng-controller="IndexesController" ng-model="searchParameters.indexName">
				<option ng-repeat="index in indexes" value="{{index}}">{{index}}</option>
			</select>
		</td>
	</tr>
	<tr>
		<td>Text NAME : </td>
		<td colspan="2"><input id="NAME" name="NAME" ng-model="NAME" value="NAME"></td>
	</tr>
	<tr>
		<td>Text PROVINCE : </td>
		<td colspan="2"><input id="PROVINCE" name="PROVINCE" ng-model="PROVINCE" value="PROVINCE"></td>
	</tr>
	<tr>
		<td>Range CD_LGL_CO_TYP : </td>
		<td><input id="CD_LGL_CO_TYP_FROM" name="CD_LGL_CO_TYP_FROM" ng-model="CD_LGL_CO_TYP_FROM" value="CD_LGL_CO_TYP_FROM"></td>
		<td><input id="CD_LGL_CO_TYP_TO" name="CD_LGL_CO_TYP_TO" ng-model="CD_LGL_CO_TYP_TO" value="CD_LGL_CO_TYP_TO"></td>
	</tr>
	<tr>
		<td>Range EMPLOYEES_COUNT : </td>
		<td><input id="EMPLOYEES_COUNT_FROM" name="EMPLOYEES_COUNT_FROM" ng-model="EMPLOYEES_COUNT_FROM" value="EMPLOYEES_COUNT_FROM"></td>
		<td><input id="EMPLOYEES_COUNT_TO" name="EMPLOYEES_COUNT_TO" ng-model="EMPLOYEES_COUNT_TO" value="EMPLOYEES_COUNT_TO"></td>
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
