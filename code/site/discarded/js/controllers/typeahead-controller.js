function TypeaheadController($scope, $http, $timeout) {
	
	$scope.loading = false;
	$scope.search = { maxResults : 6 };
	$scope.results = new Array();
	$scope.searchString = null;
	
	$scope.convertToArray = function() {
		$scope.results = new Array();
		$scope.statistics = $scope.search.searchResults.pop();
		// Exception or no results
		if ($scope.statistics == undefined || $scope.statistics.total == undefined || $scope.statistics.total == 0) {
			return;
		}
		$scope.results = new Array();
		// Iterate through the results from the Json data
		angular.forEach($scope.search.searchResults, function(key, value) {
			$scope.results.push(key['fragment']);
		});
		return $scope.results;
	}
	
	$scope.doSearch = function(uri, searchString) {
		if (!$scope.loading) {
			$scope.loading = true;
			$scope.url = getServiceUrl(uri);
			$scope.search.fragment = true;
			$scope.search.searchStrings = [searchString];
			$scope.search.maxResults = 6;
			
			var promise = $http.post($scope.url, $scope.search);
			promise.success(function(data, status) {
				// Pop the statistics Json off the array
				$scope.search = data;
				$scope.status = status;
				$scope.convertToArray();
			});
			promise.error(function(data, status) {
				$scope.status = status;
			});
			$scope.loading = false;
		}
		return $timeout(function() {
			return $scope.results;
		}, 250);
	};
	
	$scope.doModalResults = function() {
		// Popup the results on a modal that can be navigated with a mouse or the keyboard
	};
	
}