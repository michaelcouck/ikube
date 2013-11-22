/**
 * This controller will display the actions currently being performed
 * and additionally provide a function to terminate the indexing on the 
 * action
 */
module.controller('ActionsController', function($http, $scope) {
	// The data that we will iterate over
	$scope.actions = {};
	$scope.url = getServiceUrl('/ikube/service/monitor/actions');
	// The function to get the Json from the server
	$scope.getActions = function() {
		var promise = $http.get($scope.url);
		promise.success(function(data, status) {
			
			var actions = data;
			if (!!$scope.actions) {
				if ($scope.actions.length == actions.length) {
					for (var i = 0; i < $scope.actions.length; i++) {
						actions[i].show = $scope.actions[i].show;
					}
				} else {
					$scope.doShow(actions);
				}
			} else {
				$scope.doShow(actions);
			}
			
			$scope.actions = actions;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	}
	$scope.doShow = function(actions) {
		angular.forEach(actions, function(action, index) {
			action.show = false;
		});
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
		$scope.url = getServiceUrl('/ikube/service/monitor/terminate');
		// The parameters for the terminate
		$scope.parameters = { 
			indexName : indexName
		};
		// The configuration for the request to the server
		$scope.config = { params : $scope.parameters };
		// And terminate the indexing for the index
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	
});