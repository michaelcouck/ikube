/**
 * This controller will just populate the page with the property files data, i.e. the
 * absolute path to the file and the contents of the file in a text area that can be posted
 * back to the server 
 */
module.controller('PropertiesController', function($http, $scope) {
	// The map of files and contents
	$scope.propertyFiles = {};
	$scope.url = getServiceUrl('/ikube/service/monitor/get-properties');
	
	$scope.getProperties = function() {
		var promise = $http.get($scope.url);
		promise.success(function(data, status) {
			$scope.status = status;
			$scope.propertyFiles = data;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	
	$scope.getProperties();
	
	$scope.setProperties = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/set-properties');
		$scope.headers = { headers: { 'Content-Type' : 'application/json' } };
		var promise = $http.post($scope.url, $scope.propertyFiles, $scope.headers);
		promise.success(function(data, status) {
			$scope.entity = data;
			$scope.status = status;
			// alert('Properties updated for file : ' + file);
		});
		promise.error(function(data, status) {
			// alert('Problem accessing the data : ' + status);
			$scope.status = status;
		});
	};
});