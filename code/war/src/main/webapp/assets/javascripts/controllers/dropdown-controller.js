module.controller('DropdownCtrl', function($scope) {
	$scope.items = [ "The first choice!", "And another choice for you.", "but wait! A third!" ];
});

//module.controller('ActiveController', function($scope, $location) {
//	$scope.tog = 1;
//	$scope.menu = 1;
//	
//	$scope.pageRequested = function(page) {
//		// alert('Location : ' + $location.absUrl());
//		return $location.absUrl().indexOf(page) > -1;
//	};
//	
//});