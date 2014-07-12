/**
 * This controller will display the actions currently being performed
 * and additionally provide a function to terminate the indexing on the 
 * action
 */
module.controller('ActionsController', function($http, $scope) {
	// The data that we will iterate over
	$scope.actions = {};
	// The function to get the Json from the server
	$scope.getActions = function() {
		var url = getServiceUrl('/ikube/service/monitor/actions');
		var promise = $http.get(url);
		promise.success(function(data, status) {
			$scope.doShow($scope.actions, data);
			$scope.actions = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	$scope.doShow = function(scopeActions, resultActions) {
		if (!!scopeActions && !!resultActions) {
			angular.forEach(scopeActions, function(scopeAction, index) {
				angular.forEach(resultActions, function(resultAction, index) {
					if (scopeAction.indexName === resultAction.indexName) {
						resultAction.show = scopeAction.show;
					}
				});
			});
		}
	};
	// Execute the action in startup
	$scope.getActions();
	// Refresh from time to time
	setInterval(function() {
		$scope.getActions();
	}, refreshInterval);
	
	$scope.direction = true;
	$scope.orderProp = "server.address";
	$scope.sort = function(column) {
		if ($scope.orderProp === column) {
			$scope.direction = !$scope.direction;
		} else {
			$scope.orderProp = column;
			$scope.direction = true;
		}
	};
	
	// This function will send a terminate event to the cluster
	$scope.terminateAction = function(indexName) {
		var url = getServiceUrl('/ikube/service/monitor/terminate');
		// The parameters for the terminate
		var parameters = { 
			indexName : indexName
		};
		// The configuration for the request to the server
		var config = { params : parameters };
		// And terminate the indexing for the index
		var promise = $http.post(url, config);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	
});