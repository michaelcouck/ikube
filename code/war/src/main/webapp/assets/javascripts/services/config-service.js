/**
 * This is a service where configuration parameters can be specified for the controllers and 
 * any other artifacts that need parameters for configuration.
 */
ikubeServices.service('configService', function($rootScope) {
	
	// The quick search in the header file
	$rootScope.quickSearchConfig = {
		name : 'Quick search',
		uri : '/ikube/service/search/json/all',
		resultsBuilder : 'resultsBuilderService'
	};
	
	// The search form autocomplete configuration
	$rootScope.searchFormConfig = {
		name : 'Search',
		uri : '/ikube/service/auto',
		resultsBuilder : 'autocompleteResultsBuilderService',
		indexName : 'autocomplete',
		searchFields : ['word'],
		emitHierarchyFunction : 'doSearch'
	};
	
	// The Twitter search form autocomplete configuration
	$rootScope.searchTwitterFormConfig = {
		name : 'Twitter search',
		uri : '/ikube/service/auto',
		resultsBuilder : 'autocompleteResultsBuilderService',
		indexName : 'autocomplete',
		searchFields : ['word'],
		emitHierarchyFunction : 'setSearchStrings'
	};
	
	/**
	 * This function gets the configuration based on the name.
	 */
	this.getConfig = function(name) {
		return $rootScope[name];
	};
});