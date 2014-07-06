/**
 * This controller will get the api details from the server, and make the
 * Json objects available for display.
 */
module.controller('ApisController', function($http, $scope) {

	$scope.apis = undefined;

    /**
     * Gets the api documentation from the server.
     */
	$scope.doApis = function() {
		$scope.url = getServiceUrl('/ikube/service/api/apis');
		var promise = $http.get($scope.url);
		promise.success(function(data, status) {
			$scope.apis = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
    $scope.doApis();

});