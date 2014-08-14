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
            angular.forEach($scope.apis, function(api, index) {
                angular.forEach(api.apiMethods, function(apiMethod, index) {
                    apiMethod.consumes = $scope.formatJSON(apiMethod.consumes, 2);
                    apiMethod.produces = $scope.formatJSON(apiMethod.produces, 2);
                });
            });
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};

    $scope.formatJSON = function(input, indent) {
        if (!input || input.length == 0) {
            return '';
        }
        else {
            // var parsedData = JSON.parse(input);
            return JSON.stringify(input, null, indent);
        }
    };

    $scope.doApis();

});