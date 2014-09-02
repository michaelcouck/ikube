/**
 * This controller will get the api details from the server, and make the
 * Json objects available for display.
 */
module.controller('ApisController', function($http, $scope) {

	$scope.apis = undefined;
    $scope.visible = {};
    $scope.api = undefined;
    $scope.apiMethod = undefined;

    /**
     * This method collapses the tag specified.
     *
     * @param api the arbitrary name of the tag to collapse, or in fact expand, depending on the
     * state of the tag at the time
     */
    $scope.toggleVisibility = function(api) {
        $scope.visible[api] = !$scope.visible[api];
    };

    $scope.toggleVisibilityAll = function(visibility) {
        angular.forEach($scope.visible, function(value, key) {
            $scope.visible[key] = visibility;
        });
    };

    /**
     * This function sets the api that is being viewed, or clicked, as the case may be, for
     * child pages, and other fragments that are not in the same block as the parent.
     *
     * @param api the api to set as the current one
     */
    $scope.setApi = function(api) {
        $scope.api = api;
    };

    /**
     * This function sets the method that is to be current, making it available for the other
     * fragments in the page to access and potentially display the attributes of.
     *
     * @param apiMethod the method to make as the current one being viewed
     */
    $scope.setApiMethod = function(apiMethod) {
        $scope.apiMethod = apiMethod;
    };

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