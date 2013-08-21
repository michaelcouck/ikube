<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<title>Ikube Example Auto-complete</title>
	<link rel="stylesheet" type="text/css" href="<c:url value="/js/bootstrap-combined.min.css" />">
	<script type='text/javascript' src="<c:url value="/js/angular.js" />"></script>
	<script type='text/javascript' src="<c:url value="/js/ui-bootstrap-tpls-0.4.0.js" />"></script>

<script type='text/javascript'>
	//<![CDATA[ 
	angular.module('ikube-autocomplete', [ 'ui.bootstrap' ]);
	function TypeaheadController($scope, $http, $timeout) {
		$scope.selected = undefined;
		// Go to the web service for the results
		$scope.data = null;
		$scope.results = new Array();
		$scope.doSearch = function(selected) {
			$scope.url = '/ikube/service/search/json/single';
			// The form parameters we send to the server
			$scope.searchParameters = {
				indexName : 'twitter',
				searchStrings : selected,
				searchFields : 'contents',
				fragment : true,
				firstResult : 0,
				maxResults : 10
			};
			// The configuration for the request to the server for the results
			$scope.config = {
				params : $scope.searchParameters
			};
			var promise = $http.get($scope.url, $scope.config);
			promise.success(function(data, status) {
				// Pop the statistics Json off the array
				$scope.data = data;
				$scope.status = status;
				$scope.statistics = $scope.data.pop();
				$scope.convertToArray($scope.data);
			});
			promise.error(function(data, status) {
				$scope.status = status;
			});
			$scope.results = new Array();
			// Convert all the data to an array for the auto complete
			$scope.convertToArray = function(data) {
				var total = $scope.statistics.total;
				// Exception or no results
				if (total == null || total == 0) {
					$scope.searchParameters.firstResult = 0;
					$scope.endResult = 0;
					return;
				}
				// Iterate through the results from the Json data
				for (var key in data) {
					$scope.results.push(data[key]['fragment']);
				}
			}
			// Wait for a while for the server to return some data, note 
			// that if the server is still too slow you can add more time to the timeout
			return $timeout(function() {
				return $scope.results;
			}, 250);
		};
	}
	//]]>
</script>
</head>

<body ng-app="ikube-autocomplete">
	<div class='container-fluid' ng-controller="TypeaheadController">
		<pre>Model: {{selected| json}}</pre>
		<!--  | filter:$viewValue -->
		<input 
			type="text" 
			ng-model="selected"
			typeahead="result for result in doSearch($viewValue)"
			typeahead-min-length="3" 
			typeahead-wait-ms="250">
	</div>
</body>

</html>