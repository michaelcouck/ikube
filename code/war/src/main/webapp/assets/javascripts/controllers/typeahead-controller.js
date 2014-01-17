/**
 * This is a relatively generic anto-complete controller. It will take a uri(for the rest call), and
 * build the object to post to the Json rest service, specifically a search object from the model 
 * package. The search object will then be populated with the strings and fields that are to be 
 * searched in the index. 
 * 
 * @author Michael Couck
 * @since 10-11-2013
 * 
 * @param $scope 
 * @param $http
 * @param $injector
 * @param $timeout
 * @param $log
 */
function TypeaheadController($scope, $http, $injector, $timeout, $log) {
	
	// The configuration object with the uri and other parameters
	$scope.config = undefined;
	// We don't want too many results, looks bad
	$scope.search = { 
		maxResults : 7,
		fragment : true
	};
	// The results that will be created from the response from the server
	$scope.results = [];
	// The search string that we will send for searching against
	$scope.searchString = null;
	
	// This function converts the Json data which is a list of 
	// maps to an array that can be displayed in the 'drop down'. The fragments
	// from the results are taken and shortened to +- 120 characters
	$scope.convertToArray = function() {
		// $scope.results = new Array();
		if (!!$scope.search && !!$scope.search.searchResults) {
			$scope.typeStatistics = $scope.search.searchResults.pop();
			// Exception or no results
			if (!!$scope.typeStatistics && !!$scope.typeStatistics.total && $scope.typeStatistics.total > 0) {
				$scope.results = $injector.get($scope.config.resultsBuilder).buildResults($scope.search.searchResults);
			}
		}
		return $scope.results;
	}
	
	// We add a watch so that when a selection is made from the drop down,
	// which is a fragment, and has html tags in it, we can remove the html before
	// passing the string to the caller that will do the search
	$scope.$watch('searchString', function() {
		$scope.searchString = $scope.stripTags($scope.searchString);
		$scope.search.searchStrings = [$scope.searchString];
		// $log.log('Search string : ' + $scope.searchString);
		// $scope.emit();
	});
	
	// Removes html tags from a string using the browser
	$scope.stripTags = function(html) {
		var div = document.createElement("div");
		div.innerHTML = html;
		return div.innerText.trim();
	};
	
	// This function will go the post to the server, using the search object, and 
	// retrieve the results if any. To prevent concurrent searches(due to the un-synch)
	// nature of Angular we set a flag when we post, and we set a timeout that will re-try 
	// the service until there are results or there are too many re-tries
	$scope.doSearch = function() {
		$scope.results = null;
		$scope.url = getServiceUrl($scope.config.uri);
		$scope.search.searchStrings = [$scope.searchString];
		
		var promise = $http.post($scope.url, $scope.search);
		promise.success(function(data, status) {
			if (!!data.searchStrings && data.searchStrings.toString() == $scope.search.searchStrings.toString()) {
				$scope.search = data;
				$scope.status = status;
				$scope.convertToArray();
			}
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
		
		// Because http requests are asynchronous we have to wait explicitly
		// for the results to come back from the server before we pass them to the 
		// type ahead controller as they will be null to begin with
		return $scope.emit();
	};
	
	var maxRetries = 100;
	$scope.emit = function() {
		return $timeout(function() {
			// Check that we have some results and that the search strings that were searched
			// for are the same ones that the user typed last, i.e. the last search that was done
			if (!!$scope.results) {
				if (!!$scope.config.emitHierarchyFunction) {
					// $log.log('Emitting to : ' + $scope.config.emitHierarchyFunction);
					// Emit the search object to any parent controllers
					// that may be interested in the fact that the user has
					// selected a search string
					$scope.$emit($scope.config.emitHierarchyFunction, $scope.search);
				}
				return $scope.results;
			} else if (maxRetries-- > 0) {
				return $scope.emit();
			}
		}, 100);
	};
	
	/**
	 * Same as the above only the search object is populated, i.e. fields are set. 
	 */
	$scope.doConfig = function(configName) {
		$scope.config = $injector.get('configService').getConfig(configName);
		// $log.log('Configuration : ' + $scope.config.name);
		$scope.search.indexName = $scope.config.indexName;
		$scope.search.searchFields = $scope.config.searchFields;
		$scope.search.typeFields = $scope.config.typeFields;
		$scope.search.sortFields = $scope.config.sortFields;
		$scope.search.occurrenceFields = $scope.config.occurrenceFields;
	};
	
	/**
	 * This method can be called after the user selects one of the drop down results, and calls a service
	 * that will then perform further logic on the selection, like doing the actual search for the selected 
	 * search string. The service must have a single function called 'execute' that takes a single parameter.
	 * 
	 * @param name of the service to call
	 * @param parameter to pass to the function call of the service
	 */
	$scope.callService = function(name, parameter) {
		var service = $injector.get(name);
		if (!!service) {
			service.execute(parameter);
		}
	};
	
}