/**
 * This is a relatively generic anto-complete controller. It will take a uri(for the rest call), and
 * build the object to post to the Json rest service, specifically a search object from the model 
 * package. The search object will then be populated with the strings and fields that are to be 
 * searched in the index. 
 * 
 * @param $scope
 * @param $http
 * @param $injector
 * @param $timeout
 */
function TypeaheadController($scope, $http, $injector, $timeout) {
	
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
	$scope.convertToArray = function(resultsBuilderService) {
		$scope.results = new Array();
		if ($scope.search != undefined && $scope.search != null && $scope.search.searchResults != undefined && $scope.search.searchResults != null) {
			$scope.statistics = $scope.search.searchResults.pop();
			// Exception or no results
			if ($scope.statistics == undefined || $scope.statistics.total == undefined || $scope.statistics.total == 0) {
				return;
			}
			$scope.results = $injector.get(resultsBuilderService).buildResults($scope.search.searchResults);
		}
		return $scope.results;
	}
	
	// This function will go the post to the server, using the search object, and 
	// retrieve the results if any. To prevent concurrent searches(due to the un-synch)
	// nature of Angular we set a flag when we post, and we set a timeout that will re-try 
	// the service until there are results or there are too many re-tries
	$scope.doSearch = function(uri, resultsBuilderService) {
		if (!$scope.loading) {
			$scope.results = null;
			$scope.loading = true;
			$scope.url = getServiceUrl(uri);
			$scope.search.fragment = true;
			$scope.search.searchStrings = [$scope.searchString];
			$scope.search.maxResults = 6;
			
			var promise = $http.post($scope.url, $scope.search);
			promise.success(function(data, status) {
				$scope.search = data;
				$scope.status = status;
				$scope.convertToArray(resultsBuilderService);
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
					// Emit the search object to any parent controllers
					// that may be interested in the fact that the user has
					// selected a search string
					$scope.$emit('doSearch', $scope.search);
					return $scope.results;
				}, 100);
			};
			// Because http requests are asynchronous we have to wait explicitly
			// for the results to come back from the server before we pass them to the 
			// type ahead controller as they will be null to begin with
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
	/**
	 * Same as the above only the search object is populated, i.e. fields are set. 
	 */
	$scope.searchProperty = function(name, value, array) {
		if (!array) {
			$scope.search[name] = value;
		} else {
			$scope.search[name] = [value];
		}
	};
	
	/**
	 * This method can be called after the user selects one of the drop down results, and calls a service
	 * that will then perform further logic on the selection, like doing the actual search for the selected 
	 * search string. The service must have a single function called 'execute' that takes a single parameter.
	 * 
	 * @param the name of the service to call
	 * @param the parameter to pass to the function call of the service
	 */
	$scope.callService = function(name, parameter) {
		var service = $injector.get(name);
		if (!!service) {
			service.execute(parameter);
		}
	};
	
}