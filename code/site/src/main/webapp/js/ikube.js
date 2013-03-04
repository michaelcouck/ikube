/** Note: This file must be loaded after all the other JavaScript files. */

jQuery.support.cors = true;

/**
 * This is the main Angular module for the iKube site on the 
 * client. This module will spawn and create the controllers and other
 * artifacts as required.
 */
var module = angular.module('site', []);

// The controller that does the search
var searcherController = module.controller('SearcherController', function($http, $scope, $location, $rootScope) {
			
	// The model data that we bind to in the form
	$scope.pageBlock = 10; // Results per page
	$scope.endResult = 0;
	$scope.data = {};
	$scope.status = null;
		
	$scope.statistics = {};
	$scope.pagination = [];
			
	// The form parameters we send to the server
	$scope.searchParameters = { 
		indexName : 'ikube',
		searchStrings : '', 
		searchFields : 'content', 
		typeFields : 'string', 
		fragment : true,
		firstResult : 0,
		maxResults : $scope.pageBlock
	};
			
	// The configuration for the request to the server for the results
	$scope.config = { params : $scope.searchParameters };
			
	// Go to the web service for the results. The parameter sets the first resultindex
	// in the Lucene index, i.e. for the first search the first result would be 1 but for the 
	// 10 th page the first result would then be 100 probably
	$scope.doSearch = function(firstResult) {
		var searchString = document.getElementById('search-form').searchString.value;
		// Numeric search against all the fields
		$scope.url = 'http://www.ikube.be/ikube/service/search/json/complex';
		$scope.searchParameters.firstResult = firstResult;
		$scope.searchParameters['searchStrings'] = searchString;
		delete $http.defaults.headers.common['X-Requested-With'];
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			// Pop the statistics Json off the array
			$scope.data = data;
			$scope.status = status;
			$scope.statistics = $scope.data.pop();
			$scope.doPagination($scope.data);
		});
		promise.error(function(data, status, config, errorResponse) {
			$scope.status = status;
			alert('Search error : sstatus : ' + status + ', response : ' + errorResponse);
		});
	};
			
	// Creates the Json pagination array for the next pages in the search
	$scope.doPagination = function(data) {
		$scope.pagination = [];
		var total = $scope.statistics.total;
		// Exception or no results
		if (total == null || total == 0) {
			$scope.searchParameters.firstResult = 0;
			$scope.endResult = 0;
			return;
		}
		// We just started a search and got the first results
		var pages = total / $scope.pageBlock;
		// Create one 'page' for each block of results
		for (var i = 0; i < pages && i < $scope.pageBlock; i++) {
			var firstResult = i * $scope.pageBlock;
			var active = firstResult == $scope.searchParameters.firstResult ? 'black' : 'blue';
			$scope.pagination[i] = { page : i, firstResult : firstResult, active : active };
		};
		// Find the 'to' result being displayed
		var modulo = total % $scope.pageBlock;
		$scope.endResult = $scope.searchParameters.firstResult + modulo == total ? total : $scope.searchParameters.firstResult + $scope.pageBlock;
	};
});

searcherController.$inject = ['$scope', '$routeParams', '$filter', 'storage', '$location'];

/**
 * This function will track the page view for Google Analytics.
 */ 
function track() {
	try {
		var pageTracker = _gat._getTracker("UA-13044914-4");
		pageTracker._trackPageview();
	} catch (err) {
		// document.write('<!-- ' + err + ' -->');
	}
}

function writeDate() {
	var d = new Date();
	document.write(d.toLocaleTimeString());
	document.write(' ');
	document.write(d.toLocaleDateString());
}

var ua = navigator.userAgent.toLowerCase();
var check = function(r) {
    return r.test(ua);
};
var DOC = document;
var isStrict = DOC.compatMode == "CSS1Compat";
var isOpera = check(/opera/);
var isChrome = check(/chrome/);
var isWebKit = check(/webkit/);
var isSafari = !isChrome && check(/safari/);
var isSafari2 = isSafari && check(/applewebkit\/4/); // unique to
// Safari 2
var isSafari3 = isSafari && check(/version\/3/);
var isSafari4 = isSafari && check(/version\/4/);
var isIE = !isOpera && check(/msie/);
var isIE7 = isIE && check(/msie 7/);
var isIE8 = isIE && check(/msie 8/);
var isIE9 = isIE && check(/msie 9/);
var isIE6 = isIE && !isIE7 && !isIE8;
var isGecko = !isWebKit && check(/gecko/);
var isGecko2 = isGecko && check(/rv:1\.8/);
var isGecko3 = isGecko && check(/rv:1\.9/);
var isBorderBox = isIE && !isStrict;
var isWindows = check(/windows|win32/);
var isMac = check(/macintosh|mac os x/);
var isAir = check(/adobeair/);
var isLinux = check(/linux/);
var isSecure = /^https/i.test(window.location.protocol);
var isIE7InIE8 = isIE7 && DOC.documentMode == 7;

var jsType = '', browserType = '', browserVersion = '', osName = '';
var ua = navigator.userAgent.toLowerCase();
var check = function(r) {
    return r.test(ua);
};

if (isWindows) {
	osName = 'Windows';
	if (check(/windows nt/)) {
		var start = ua.indexOf('windows nt');
		var end = ua.indexOf(';', start);
		osName = ua.substring(start, end);
	}
} else {
	osName = isMac ? 'Mac' : isLinux ? 'Linux' : 'Other';
} 

if (isIE) {
	browserType = 'IE';
	jsType = 'IE';

	var versionStart = ua.indexOf('msie') + 5;
	var versionEnd = ua.indexOf(';', versionStart);
	browserVersion = ua.substring(versionStart, versionEnd);

	jsType = isIE6 ? 'IE6' : isIE7 ? 'IE7' : isIE8 ? 'IE8' : 'IE';
} else if (isGecko) {
	var isFF = check(/firefox/);
	browserType = isFF ? 'Firefox' : 'Others';
	;
	jsType = isGecko2 ? 'Gecko2' : isGecko3 ? 'Gecko3' : 'Gecko';

	if (isFF) {
		var versionStart = ua.indexOf('firefox') + 8;
		var versionEnd = ua.indexOf(' ', versionStart);
		if (versionEnd == -1) {
			versionEnd = ua.length;
		}
		browserVersion = ua.substring(versionStart, versionEnd);
	}
} else if (isChrome) {
	browserType = 'Chrome';
	jsType = isWebKit ? 'Web Kit' : 'Other';

	var versionStart = ua.indexOf('chrome') + 7;
	var versionEnd = ua.indexOf(' ', versionStart);
	browserVersion = ua.substring(versionStart, versionEnd);
} else {
	browserType = isOpera ? 'Opera' : isSafari ? 'Safari' : '';
}

function doFocus(elementId) {
	var element = document.getElementById(elementId);
	if (element != null) {
		// Bug in IE can't focus ... :)
		if (!isIE) {
			element.focus();
		}
	}
}

function popup(mylink, windowname) {
	if (!window.focus) {
		return true;
	}
	var href;
	if (typeof(mylink) == 'string') {
		href=mylink;
	} else {
		href=mylink.href;
	}
	window.open(href, windowname, 'width=750,height=463,scrollbars=yes');
	return false;
}

/**
 * This function will capitalize the first letter of a string.
 * 
 * @returns the string with the first letter capital
 */ 
String.prototype.capitalize = function() {
	return this.charAt(0).toUpperCase() + this.slice(1);
};

/**
 * This function builds the url to the rest search service.
 * 
 * @param the path part of the url  
 * @returns the url to the search rest web service
 */
function getServiceUrl(path) {
	var url = [];
	url.push(window.location.protocol);
	url.push('//');
	url.push(window.location.host);
	url.push(path);
	return url.join('');
}