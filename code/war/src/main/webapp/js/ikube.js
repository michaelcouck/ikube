/** Note: This file must be loaded after all the other JavaScript files. */

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

/** The global refresh variable. */
var refreshInterval = 15000;
var chartRefreshInterval = 5000;

/**
 * This is the main Angular module for the iKube application on the 
 * client. This module will spawn and create the controllers and other
 * artifacts as required.
 */
var module = angular.module('ikube', []);

/** This directive will draw and update the searching performance graph. */
module.directive('searching', function($http) {
	return {
		restrict : 'A',
		scope : true,
		link : function($scope, $elm, $attr) {
			$scope.options = { 
				title : 'Searching performance',
				legend : { position : 'top', textStyle : { color : 'black', fontSize : 12 } } };
			$scope.drawSearchingChart = function() {
				$scope.url = getServiceUrl('/ikube/service/monitor/searching');
				var promise = $http.get($scope.url);
				promise.success(function(data, status) {
					$scope.status = status;
					var data = google.visualization.arrayToDataTable(data);
					var searchingChart = new google.visualization.LineChart($elm[0]);
					searchingChart.draw(data, $scope.options);
				});
				promise.error(function(data, status) {
					$scope.status = status;
				});
			}
			// Initially draw the chart from the server data
			$scope.drawSearchingChart();
			// And re-draw it every few seconds to give the live update feel
			setInterval(function() {
				$scope.drawSearchingChart();
			}, chartRefreshInterval);
		}
	}
});

/** This directive will draw and update the indexing performance graph. */
module.directive('indexing', function($http) {
	return {
		restrict : 'A',
		scope : true,
		link : function($scope, $elm, $attr) {
			$scope.options = { 
				title : 'Indexing performance',
				legend : { position : 'top', textStyle : { color : 'black', fontSize : 12 } } };
			$scope.drawIndexingChart = function() {
				$scope.url = getServiceUrl('/ikube/service/monitor/indexing');
				var promise = $http.get($scope.url);
				promise.success(function(data, status) {
					$scope.status = status;
					var data = google.visualization.arrayToDataTable(data);
					var indexingChart = new google.visualization.LineChart($elm[0]);
					indexingChart.draw(data, $scope.options);
				});
				promise.error(function(data, status) {
					$scope.status = status;
				});
			}
			// Initially draw the chart from the server data
			$scope.drawIndexingChart();
			// And re-draw it every few seconds to give the live update feel
			setInterval(function() {
				$scope.drawIndexingChart();
			}, chartRefreshInterval);
		}
	}
});

/** Load the Google visual after the directives to avoid some kind of recursive lookup. */
google.load('visualization', '1', { packages : [ 'corechart' ] });

/** This controller will get the server data from the grid. */
module.controller('ServersController', function($http, $scope) {
	$scope.servers = [];
	
	$scope.refreshServers = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/servers');
		var promise = $http.get($scope.url);
		promise.success(function(data, status) {
			$scope.servers = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	}
	$scope.refreshServers();
	setInterval(function() {
		$scope.refreshServers();
	}, refreshInterval);
});

/**
 * This controller will display the acitons currently being performed
 * and additionally provide a function to terminate the indexing on the 
 * action
 */
module.controller('ActionsController', function($http, $scope) {
	// The data that we will iterate over
	$scope.actions = {};
	$scope.url = getServiceUrl('/ikube/service/monitor/actions');
	// The function to get the Json from the server
	$scope.getActions = function() {
		var promise = $http.get($scope.url);
		promise.success(function(data, status) {
			$scope.actions = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	}
	// Execute the action in startup
	$scope.getActions();
	// Refresh from time to time
	setInterval(function() {
		$scope.getActions();
	}, refreshInterval);
	// This function will send a terminate event to the cluster
	$scope.terminateIndexing = function(indexName) {
		if (confirm('Terminate indexing of index : ' + indexName)) {
			$scope.url = getServiceUrl('/ikube/service/monitor/terminate');
			// The parameters for the terminate
			$scope.parameters = { 
				indexName : indexName
			};
			// The configuration for the request to the server
			$scope.config = { params : $scope.parameters };
			// And terminate the indexing for the index
			var promise = $http.get($scope.url, $scope.config);
			promise.success(function(data, status) {
				$scope.status = status;
			});
			promise.error(function(data, status) {
				$scope.status = status;
			});
			// $scope.getActions();
		}
	}
});

module.controller('StartupController', function($http, $scope) {
	$scope.startupAll = function() {
		if (confirm('Re-start all schedules and actions in the cluster : ')) {
			$scope.url = getServiceUrl('/ikube/service/monitor/startup-all');
			$scope.parameters = {};
			// The configuration for the request to the server
			$scope.config = { params : $scope.parameters };
			// And start all the schedules again
			var promise = $http.get($scope.url, $scope.config);
			promise.success(function(data, status) {
				$scope.status = status;
			});
			promise.error(function(data, status) {
				$scope.status = status;
			});
		}
	}
	$scope.terminateAll = function() {
		if (confirm('Terminate all schedules and actions in the cluster : ')) {
			$scope.url = getServiceUrl('/ikube/service/monitor/terminate-all');
			$scope.parameters = {};
			// The configuration for the request to the server
			$scope.config = { params : $scope.parameters };
			// And terminate the schedules in the cluster
			var promise = $http.get($scope.url, $scope.config);
			promise.success(function(data, status) {
				$scope.status = status;
			});
			promise.error(function(data, status) {
				$scope.status = status;
			});
		}
	}
});

/** This controller gathers the index context data from the server for presentation. */
module.controller('IndexContextsController', function($http, $scope) {
	$scope.indexContexts = [];
	
	$scope.sortField = 'name';
	$scope.descending = false;
	$scope.refreshIndexContexts = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/index-contexts');
		$scope.parameters = { 
			sortField : $scope.sortField,
			descending : $scope.descending
		};
		$scope.config = { params : $scope.parameters };
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.indexContexts = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	}
	$scope.sortIndexContexts = function(sortField) {
		if ($scope.sortField == sortField) {
			// Switch the order
			$scope.descending = !$scope.descending;
		} else {
			// Order ascending and switch the field
			$scope.descending = false;
			$scope.sortField = sortField;
		}
		// alert('Sort field : ' + sortField + ', ' + ($scope.sortField == sortField) + ', ' + $scope.descending);
		$scope.refreshIndexContexts();
	};
	// Immediately refresh the data
	$scope.refreshIndexContexts();
	// Refresh the index contexts every so often
	setInterval(function() {
		$scope.refreshIndexContexts();
	}, refreshInterval);
	// This function will publish a start event in the cluster
	$scope.startIndexing = function(indexName) {
		if (confirm('Start indexing of index : ' + indexName)) {
			$scope.url = getServiceUrl('/ikube/service/monitor/start');
			// The parameters for the start
			$scope.parameters = { 
				indexName : indexName
			};
			// The configuration for the request to the server
			$scope.config = { params : $scope.parameters };
			// And terminate the indexing for the index
			var promise = $http.get($scope.url, $scope.config);
			promise.success(function(data, status) {
				$scope.status = status;
			});
			promise.error(function(data, status) {
				$scope.status = status;
			});
		}
	}
	
	// This function will delete the index completely on the file system
	$scope.deleteIndex = function(indexName) {
		if (confirm('Delete index completely for index : ' + indexName)) {
			$scope.url = getServiceUrl('/ikube/service/monitor/delete-index');
			// The parameters for the delete of the index
			$scope.parameters = { 
				indexName : indexName
			};
			// The configuration for the request to the server
			$scope.config = { params : $scope.parameters };
			// And delete the index
			var promise = $http.get($scope.url, $scope.config);
			promise.success(function(data, status) {
				$scope.status = status;
			});
			promise.error(function(data, status) {
				$scope.status = status;
				alert('Error sending delete message : ' + status);
			});
		}
	}
});

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
});

function writeDate() {
	var d = new Date();
	document.write(d.toLocaleTimeString());
	document.write(' ');
	document.write(d.toLocaleDateString());
}

function addAutoComplete(inputField) {
	inputField.autocomplete({
		source : getServiceUrl("/ikube/service/auto/complete")
	});
}

function printMethods(object) {
	if (object == null) {
		return;
	}
	for (var m in object) {
		alert('M : ' + m);
	}
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