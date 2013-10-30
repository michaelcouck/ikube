/**
 * This is a relatively generic anto-complete controller. It will take a uri(for the rest call), and
 * build the object to post to the Json rest service, specifically a search object from the model 
 * package. The search object will then be populated with the strings and fields that are to be 
 * searched in the index. 
 * 
 * @param $scope
 * @param $http
 * @param $timeout
 */
function TypeaheadController($scope, $http, $timeout) {
	
	// Flag to prevent multiple calls concurrently
	$scope.loading = false;
	// We don't want too many results, looks bad
	$scope.search = { maxResults : 6 };
	// The results that will be created from the response from the server
	$scope.results = new Array();
	// The search string that we will send for searching against
	$scope.searchString = null;
	
	// This function converts the Json data which is a list of 
	// maps to an array that can be displayed in the 'drop down'. The fragments
	// from the results are taken and shortened to +- 120 characters
	$scope.convertToArray = function() {
		$scope.results = new Array();
		if ($scope.search != undefined && $scope.search != null && $scope.search.searchResults != undefined && $scope.search.searchResults != null) {
			$scope.statistics = $scope.search.searchResults.pop();
			// Exception or no results
			if ($scope.statistics == undefined || $scope.statistics.total == undefined || $scope.statistics.total == 0) {
				return;
			}
			$scope.results = new Array();
			// Iterate through the results from the Json data
			angular.forEach($scope.search.searchResults, function(key, value) {
				var string = key['fragment'].substring(0, 120);
				$scope.results.push(string);
			});
		}
		return $scope.results;
	}
	
	// This function will go the post to the server, using the search object, and 
	// retrieve the results if any. To prevent concurrent searches(due to the un-synch)
	// nature of Angular we set a flag when we post, and we set a timeout that will re-try 
	// the service until there are results or there are too many re-tries
	$scope.doSearch = function(uri) {
		if (!$scope.loading) {
			$scope.loading = true;
			$scope.url = getServiceUrl(uri);
			$scope.search.fragment = true;
			$scope.search.searchStrings = [$scope.searchString];
			$scope.search.maxResults = 6;
			
			var promise = $http.post($scope.url, $scope.search);
			promise.success(function(data, status) {
				$scope.search = data;
				$scope.status = status;
				$scope.convertToArray();
				$scope.loading = false;
			});
			promise.error(function(data, status) {
				$scope.status = status;
				$scope.loading = false;
			});
			
			var maxRetries = 5;
			$scope.wait = function() {
				return $timeout(function() {
					if (maxRetries-- > 0 && $scope.result == null || $scope.results == undefined) {
						return $scope.wait();
					}
					return $scope.results;
				}, 250);
			};
			return $scope.wait();
		}
	};
	
	/**
	 * This function will set a value to a property in the scope by name.
	 * @param the name of the property in the current scope
	 * @param the value to set for the property
	 */
	$scope.property = function(name, value) {
		$scope[name] = value;
	};
	
	$scope.doModalResults = function() {
		// Popup the results on a modal that can be navigated with a mouse or the keyboard
	};
	
}