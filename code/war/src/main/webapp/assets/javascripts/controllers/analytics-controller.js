/**
 * @author Michael Couck
 * @since 24-11-2013
 * 
 * @param $scope
 * @param $http
 * @param $injector
 * @param $timeout
 */
module.controller('AnalyticsController', function($http, $scope, $injector, $timeout) {
	
	$scope.status = 200;
	$scope.analysis = {};
	$scope.analyzer;
	$scope.analyzers;
	
	$scope.doAnalysis = function() {
		var url = getServiceUrl('/ikube/service/analyzer/analyze');
		var promise = $http.post(url, $scope.analysis);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
		$scope.waaaaaiiiiitttt = function() {
			return $timeout(function() {
				// Bla...
			}, 100);
		};
		return $scope.waaaaaiiiiitttt();
	};
	
	$scope.doTrain = function() {
		var url = getServiceUrl('/ikube/service/analyzer/analyze');
		var promise = $http.post(url, $scope.analysis);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	
	$scope.doAnalyzers = function() {
		var url = getServiceUrl('/ikube/service/monitor/analyzers');
		var promise = $http.get(url);
		promise.success(function(data, status) {
			$scope.status = status;
			$scope.analyzers = data;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	$scope.doAnalyzers();
	
});