//module.controller('TypeaheadController', function($scope, $http, $timeout) {
function TypeaheadController($scope, $http, $timeout) {
	$scope.search = {
		maxResults : 10
	};
	$scope.results = new Array();
	$scope.doSearch = function(uri, searchString) {
		$scope.url = getServiceUrl(uri);
		$scope.search.fragment = true;
		$scope.search.searchStrings = [searchString];
		// alert('Url : ' + $scope.url);
		var promise = $http.post($scope.url, $scope.search);
		promise.success(function(data, status) {
			// Pop the statistics Json off the array
			$scope.search = data;
			$scope.status = status;
			$scope.convertToArray();
			// alert('Array : ' + $scope.results);
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
		$scope.results = new Array();
		// Convert all the data to an array for the auto complete
		$scope.convertToArray = function() {
			$scope.statistics = $scope.search.searchResults.pop();
			// Exception or no results
			if ($scope.statistics == undefined || $scope.statistics.total == undefined || $scope.statistics.total == 0) {
				return;
			}
			// Iterate through the results from the Json data
			for (var key in $scope.search.searchResults) {
				$scope.results.push($scope.search.searchResults['fragment']);
			}
		}
		// Wait for a while for the server to return some data, note 
		// that if the server is still too slow you can add more time to the timeout
		return $timeout(function() {
			return $scope.results;
		}, 250);
	};
}