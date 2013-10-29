var ikubeServices = angular.module('ikube-services', []);
ikubeServices.service('databaseService', function($rootScope, $http, $timeout) {
	this.getEntities = function(klass, startIndex, endIndex) {
		var entities = [];
		var url = getServiceUrl('/ikube/service/database/entities');
		var parameters = {
				class : klass,
				startIndex : startIndex,
				endIndex : endIndex
		};
		// The configuration for the request to the server
		var config = { params : parameters };
		var promise = $http.get(url, config);
		promise.success(function(data, status) {
			entities = data;
		});
		promise.error(function(data, status) {
			// alert('Problem accessing the data : ' + status);
		});
		return $timeout(function() {
			// alert('Gor results for : ' + klass + ', ' + startIndex + ', ' + endIndex + ', ' + entities);
			return entities;
		}, 250);
	};
});

ikubeServices.service('CreateController', function($rootScope, $http) {
	this.createEntity = function(entity) {
		var url = getServiceUrl('/ikube/service/database/entity/create');
		var headers = { Accept : 'application/json' };
		$scope.config = { headers : headers };
		var promise = $http.post(url, entity, config);
		promise.success(function(data, status) {
			// Should be no return from a post
		});
		promise.error(function(data, status) {
			// alert('Problem creating the entity : ' + status);
		});
	}
	
});

//function factory('databaseFactory', function($rootScope) {
//	return {
//		getEntities : function() {
//			alert('Factory');
//			return "Hello world";
//		};
//	};
//});
//
//function provider('databaseProvider', function($rootScope) {
//	this.$get = function() {
//		return {
//			getEntities : function() {
//				alert('Provider');
//				return "Hello world";
//			};
//		};
//	};
//});