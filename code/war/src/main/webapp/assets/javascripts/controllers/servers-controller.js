/** This controller will get the server data from the grid. */
module.controller('ServersController', function($http, $scope, $timeout, databaseService) {
	$scope.servers = [];
	$scope.entities = undefined;
	$scope.terminateThreads = false;
	$scope.terminateThreadsConfirmed = false;
	$scope.terminateThrottling = false;
	$scope.terminateThrottlingConfirmed = false;
	
	$scope.refreshServers = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/servers');
		var promise = $http.get($scope.url);
		promise.success(function(data, status) {
			$scope.doShow($scope.servers, data);
			$scope.servers = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	$scope.doShow = function(scopeServers, resultServers) {
		if (!!scopeServers && !!resultServers) {
			angular.forEach(scopeServers, function(scopeServer, index) {
				angular.forEach(resultServers, function(resultServer, index) {
					if (scopeServer.address === resultServer.address) {
						resultServer.show = scopeServer.show;
					}
				});
			});
		}
	};
	$scope.refreshServers();
	setInterval(function() {
		$scope.refreshServers();
        $timeout(function() {
            $scope.$apply();
        }, 1000);
	}, refreshInterval);
	
	$scope.startupAll = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/startup-all');
		$scope.parameters = {};
		// The configuration for the request to the server
		$scope.config = { params : $scope.parameters };
		// And start all the schedules again
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	
	$scope.terminateAll = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/terminate-all');
		$scope.parameters = {};
		// The configuration for the request to the server
		$scope.config = { params : $scope.parameters };
		// And terminate the schedules in the cluster
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	
	$scope.date = function(millis) {
		return new Date(millis).toLocaleTimeString();
	};
	
	$scope.toggleThreadsRunning = function() {
		if ($scope.server.threadsRunning) {
			$scope.terminateAll();
		} else {
			$scope.startupAll();
		}
		$scope.servers[0].threadsRunning = !$scope.server[0].threadsRunning;
	};
	
	$scope.toggleCpuThrottling = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/cpu-throttling');
		$scope.parameters = {};
		// The configuration for the request to the server
		$scope.config = { params : $scope.parameters };
		// And terminate the schedules in the cluster
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
		$scope.servers[0].cpuThrottling = !$scope.servers[0].cpuThrottling;
	};
	
	$scope.cpuLoadTooHigh = function(server) {
		return server.averageCpuLoad / server.processors > 0.9;
	};
	
	$scope.entities = databaseService.getEntities('ikube.model.Search', '0', '10');
	
});