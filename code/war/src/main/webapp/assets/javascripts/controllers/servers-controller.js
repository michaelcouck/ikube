/** This controller will get the server data from the grid. */
module.controller('ServersController', function($http, $scope, databaseService) {
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
			var servers = data;
			if (!!$scope.servers) {
				if ($scope.servers.length == servers.length) {
					for (var i = 0; i < $scope.servers.length; i++) {
						servers[i].show = $scope.servers[i].show;
					}
				} else {
					$scope.doShow(servers);
				}
			} else {
				$scope.doShow(servers);
			}
			$scope.servers = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	$scope.doShow = function(servers) {
		angular.forEach(servers, function(server, index) {
			server.show = false;
		});
	};
	$scope.refreshServers();
	setInterval(function() {
		$scope.refreshServers();
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