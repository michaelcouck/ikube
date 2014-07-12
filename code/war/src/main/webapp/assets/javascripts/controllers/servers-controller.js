/**
 * This controller will get the server data from the grid. Also this controller
 * has functions to start the thread pools and jobs and to stop them if necessary
 * also, for the whole cluster.
 */
module.controller('ServersController', function($http, $scope, $timeout, databaseService) {

    $scope.server = undefined;
	$scope.servers = [];
	$scope.entities = undefined;

    /** Start initialization */

    /**
     * Gets the latest server from the grid.
     */
	$scope.refreshServer = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/server');
		var promise = $http.get($scope.url);
		promise.success(function(data, status) {
			$scope.server = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
    /**
     * Gets all the server objects from the grid.
     */
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
    /**
     * This function allows the list to stay opened if the user opened it
     * previously and the data was then refreshed from the server again.
     */
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
	$scope.refreshServer();
	$scope.refreshServers();
	setInterval(function() {
        $scope.refreshServer();
		$scope.refreshServers();
        $timeout(function() {
            $scope.$apply();
        }, 1000);
	}, refreshInterval);
    /** End initialization. */

    /**
     * This function starts all the thread pools in the server, and the
     * pools for fork join objects. Typically this will have no effect if the
     * thread pools are still active.
     */
	$scope.startupAll = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/startup-all');
		$scope.parameters = {};
		// The configuration for the request to the server
		$scope.config = { params : $scope.parameters };
		// And start all the schedules again
		var promise = $http.post($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};

    /**
     * This function will terminate all the thread pools in the server, effectively
     * destroying and stoppint all the jobs that are currently being executed.
     */
	$scope.terminateAll = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/terminate-all');
		$scope.parameters = {};
		// The configuration for the request to the server
		$scope.config = { params : $scope.parameters };
		// And terminate the schedules in the cluster
		var promise = $http.post($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};

    /**
     * This function decides to start or stop the thread pools.
     */
	$scope.toggleThreadsRunning = function() {
        // alert('Threads running : ' + $scope.server.threadsRunning);
		if (!$scope.server.threadsRunning) {
            $scope.startupAll();
        } else {
            $scope.terminateAll();
		}
        $scope.refreshServer();
        $scope.refreshServers();
	};

    /**
     * This function goggles the cpu throttling functionality.
     */
	$scope.toggleCpuThrottling = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/cpu-throttling');
		$scope.parameters = {};
		$scope.config = { params : $scope.parameters };
		var promise = $http.post($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	
	$scope.cpuLoadTooHigh = function(server) {
		return server.averageCpuLoad / server.processors > 0.9;
	};

    $scope.date = function(millis) {
        return new Date(millis).toLocaleTimeString();
    };
	
	$scope.entities = databaseService.getEntities('ikube.model.Search', '0', '10');

});