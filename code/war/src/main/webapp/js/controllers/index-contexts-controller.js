/** This controller gathers the index context data from the server for presentation. */
module.controller('IndexContextsController', function($http, $scope) {
	$scope.indexContexts = [];
	
	$scope.sortField = 'name';
	$scope.descending = true;
	$scope.refreshIndexContexts = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/index-contexts');
		$scope.parameters = { 
			sortField : $scope.sortField,
			descending : $scope.descending
		};
		$scope.config = { params : $scope.parameters };
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.indexContexts = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	}
	// Immediately refresh the data
	$scope.refreshIndexContexts();
	// Refresh the index contexts every so often
	setInterval(function() {
		$scope.refreshIndexContexts();
	}, 60000);
	

	$scope.direction = true;
	$scope.orderProp = "name";
	$scope.sort = function(column) {
		if ($scope.orderProp === column) {
			$scope.direction = !$scope.direction;
		} else {
			$scope.orderProp = column;
			$scope.direction = true;
		}
	};
	
	// This function will publish a start event in the cluster
	$scope.startIndexing = function(indexName) {
		if (confirm('Start indexing of index : ' + indexName)) {
			$scope.url = getServiceUrl('/ikube/service/monitor/start');
			// The parameters for the start
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
		}
	}
	
	// This function will delete the index completely on the file system
	$scope.deleteIndex = function(indexName) {
		if (confirm('Delete index completely for index : ' + indexName)) {
			$scope.url = getServiceUrl('/ikube/service/monitor/delete-index');
			// The parameters for the delete of the index
			$scope.parameters = { 
				indexName : indexName
			};
			// The configuration for the request to the server
			$scope.config = { params : $scope.parameters };
			// And delete the index
			var promise = $http.get($scope.url, $scope.config);
			promise.success(function(data, status) {
				$scope.status = status;
			});
			promise.error(function(data, status) {
				$scope.status = status;
				alert('Error sending delete message : ' + status);
			});
		}
	}
	
	// This function will send a terminate event to the cluster
	$scope.terminateIndexing = function(indexName) {
		if (confirm('Terminate indexing of index : ' + indexName)) {
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
			// $scope.getActions();
		}
	}
});