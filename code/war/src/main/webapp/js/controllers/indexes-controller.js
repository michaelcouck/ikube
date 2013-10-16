// The controller that populates the indexes drop down
module.controller('IndexesController', function($http, $scope) {
	$scope.index = null;
	$scope.indexes = null;
	$scope.doIndexes = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/indexes');
		var promise = $http.get($scope.url);
		promise.success(function(data, status) {
			$scope.indexes = data;
			$scope.status = status;
		});
	}
	$scope.doIndexes();
});