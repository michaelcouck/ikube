/**
 * This is a service where configuration parameters can be specified for the controllers and 
 * any other artifacts that need parameters for configuration.
 */
ikubeServices.service('configService', function($rootScope) {
	
	// The quick search in the header file
	$rootScope.quickSearchConfig = {
		uri : '/ikube/service/search/json/all',
		resultsBuilder : 'resultsBuilderService'
	};
	
	// The search form autocomplete configuration
	$rootScope.searchFormConfig = {
		uri : '/ikube/service/search/json',
		resultsBuilder : 'autocompleteResultsBuilderService',
		indexName : 'autocomplete',
		searchFields : ['word'],
		emitHierarchyFunction : 'doSearch'
	};
	
	/**
	 * This function gets the configuration based on the name.
	 */
	this.getConfig = function(name) {
		return $rootScope[name];
	};
});