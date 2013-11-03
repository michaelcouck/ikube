ikubeServices.service('resultsBuilderService', function($rootScope) {
	this.buildResults = function(searchResults) {
		var results = new Array();
		// Iterate through the results from the Json data
		angular.forEach(searchResults, function(key, value) {
			var id = key['id'];
			var fragment = key['fragment'];
			if (!!fragment) {
				var builder = [];
				if (!!id) {
					builder.push('<b>Id : </b>');
					builder.push(id);
					builder.push(', ');
				}
				builder.push('<b>Fragment : </b>');
				builder.push(fragment);
				var result = builder.join('').substring(0, 120);
				results.push(result);
			}
		});
		return results;
	};
});

ikubeServices.service('autocompleteResultsBuilderService', function($rootScope) {
	this.buildResults = function(searchResults) {
		var results = new Array();
		angular.forEach(searchResults, function(key, value) {
			var fragment = key['fragment'];
			if (!!fragment) {
				results.push(fragment);
			}
		});
		return results;
	};
});

//function factory('someFactory', function($rootScope) {
//	return {
//		getEntities : function() {
//			alert('Factory');
//			return "Hello world";
//		};
//	};
//});
//
//function provider('someProvider', function($rootScope) {
//	this.$get = function() {
//		return {
//			getEntities : function() {
//				alert('Provider');
//				return "Hello world";
//			};
//		};
//	};
//});