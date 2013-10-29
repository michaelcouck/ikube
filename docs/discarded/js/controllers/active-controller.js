/**
 * This controller will change the status of active for all the sibling elements to 
 * not active, and the current element class to active. 
 */
module.controller('ActiveController', function($scope, $location) {
	$scope.tog = 1;
	$scope.menu = 1;
	
	$scope.pageRequested = function(page) {
		// alert('Location : ' + $location.absUrl());
		return $location.absUrl().indexOf(page) > -1;
	};
	
});