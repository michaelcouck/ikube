/**
 * @author Michael Couck
 * @since 29-09-2014
 */
module.controller('StocksController', function($scope, $http, $injector, $timeout, $log) {

	$scope.status = 200;
	$scope.createUrl = '/ikube/service/analyzer/create';
	$scope.analyzeUrl = '/ikube/service/analyzer/analyze';
	$scope.destroyUrl = '/ikube/service/analyzer/destroy';

    //

});