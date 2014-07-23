var ikubeServices = angular.module('ikube-services', []);
ikubeServices.service('databaseService', function($rootScope, $http, $timeout) {
	this.getEntities = function(klass, startIndex, endIndex) {
		var entities = [];
		return $timeout(function() {
			return entities;
		}, 250);
	};
});

ikubeServices.service('CreateController', function($rootScope, $http) {
	this.createEntity = function(entity) {
	}
});