module.controller('DatabaseController', function($http, $scope) {
	$scope.entities = null;
	$scope.doIndexContexts = function() {
		$scope.url = getServiceUrl('/ikube/service/database/entities');
		$scope.parameters = { 
			class : 'ikube.model.IndexContext',
			startIndex : 0,
			endIndex : 10
		};
		// The configuration for the request to the server
		$scope.config = { params : $scope.parameters };
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.entities = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			alert('Problem accessing the data : ' + status);
			$scope.status = status;
		});
	}
	$scope.doIndexContexts();
});

module.controller('CreateController', function($http, $scope) {
	$scope.entity = null;
	$scope.getEntity = function() {
		$scope.url = getServiceUrl('/ikube/service/database/entity');
		$scope.parameters = { 
			id : '0',
			class : 'ikube.model.IndexContext'
		};
		$scope.config = { params : $scope.parameters };
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.entity = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			alert('Problem accessing the data : ' + status);
			$scope.status = status;
		});
	}
	$scope.getEntity();
	
	$scope.createEntity = function() {
		$scope.url = getServiceUrl('/ikube/service/database/entity/create');
		$scope.headers = { 
			Accept : 'application/json'
		};
		$scope.config = { headers : $scope.headers };
		var promise = $http.post($scope.url, $scope.entity, $scope.config);
		promise.success(function(data, status) {
			$scope.entity = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			alert('Problem creating the entity : ' + status);
			$scope.status = status;
		});
	}
	
});