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
var refreshInterval = 20000;
var chartRefreshInterval = 5000;

/**
 * This is the main Angular module for the iKube application on the 
 * client. This module will spawn and create the controllers and other
 * artifacts as required.
 */
var module = angular.module('ikube', [ 'ui.bootstrap' ]);

/** This directive will draw and update the searching performance graph. */
module.directive('searching', function($http) {
	return {
		restrict : 'A',
		scope : true,
		link : function($scope, $elm, $attr) {
			$scope.options = { 
				titleTextStyle : { color: '#bbbbbb' },
				height : 200,
				width : '100%',
				backgroundColor: { fill : 'transparent' },
				legend : { position : 'top', textStyle : { color : '#bbbbbb', fontSize : 12 } },
				vAxis: { title : 'Searches',  titleTextStyle: { color: '#bbbbbb' } },
				hAxis : { textStyle : { color: '#bbbbbb' } }
			};
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
				titleTextStyle : { color: '#bbbbbb' },
				height : 200,
				width : '100%',
				backgroundColor: { fill : 'transparent' },
				legend : { position : 'top', textStyle : { color : '#bbbbbb', fontSize : 12 } },
				vAxis: { title : 'Indexing',  titleTextStyle: { color: '#bbbbbb' } },
				hAxis : { textStyle : { color: '#bbbbbb' } }
			};
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

module.directive('ng-enter', function($http) {
	return function(scope, element, attrs) {
		element.bind("keydown keypress", function(event) {
			if (event.which === 13) {
				scope.$apply(function() {
					scope.$eval(attrs.ngEnter);
				});
				event.preventDefault();
			}
		});
	};
});

/**
 * Load the Google visual after the directives to avoid some kind of recursive
 * lookup.
 */
try {
	google.load('visualization', '1', { packages : [ 'corechart' ] });
} catch (err) {
	// alert('Oops : ' + err);
}

/** This controller will get the server data from the grid. */
module.controller('ServersController', function($http, $scope) {
	$scope.server;
	$scope.servers = [];
	
	$scope.refreshServer = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/server');
		var promise = $http.get($scope.url);
		promise.success(function(data, status) {
			$scope.server = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	}
	$scope.refreshServer();
	setInterval(function() {
		$scope.refreshServer();
	}, refreshInterval);
	
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
		
		$scope.direction = true;
		$scope.orderProp = "address";
		$scope.sort = function(column) {
			if ($scope.orderProp === column) {
				$scope.direction = !$scope.direction;
			} else {
				$scope.orderProp = column;
				$scope.direction = true;
			}
		};
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
	
	$scope.date = function(millis) {
		return new Date(millis).toLocaleTimeString();
	};
	
	$scope.toggleCpuThrottling = function() {
		if (confirm('Turn the CPU throttling on/off : ')) {
			$scope.url = getServiceUrl('/ikube/service/monitor/cpu-throttling');
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
	
	$scope.cpuLoadTooHigh = function(server) {
		return server.averageCpuLoad / server.processors > 0.9;
	}
	
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
	
	$scope.direction = true;
	$scope.orderProp = "server.address";
	$scope.sort = function(column) {
		if ($scope.orderProp === column) {
			$scope.direction = !$scope.direction;
		} else {
			$scope.orderProp = column;
			$scope.direction = true;
		}
	};
});

/** This controller gathers the index context data from the server for presentation. */
module.controller('IndexContextsController', function($http, $scope) {
	$scope.indexContexts = [];
	
	$scope.sortField = 'name';
	$scope.descending = true;
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
	// Immediately refresh the data
	$scope.refreshIndexContexts();
	// Refresh the index contexts every so often
	setInterval(function() {
		$scope.refreshIndexContexts();
	}, 60000);
	

	$scope.direction = true;
	$scope.orderProp = "name";
	$scope.sort = function(column) {
		if ($scope.orderProp === column) {
			$scope.direction = !$scope.direction;
		} else {
			$scope.orderProp = column;
			$scope.direction = true;
		}
	};
	
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
	
	$scope.onSubmit = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/set-properties');
		$scope.headers = { headers: { 'Content-Type' : 'application/json' } };
		var promise = $http.post($scope.url, $scope.propertyFiles, $scope.headers);
		promise.success(function(data, status) {
			$scope.entity = data;
			$scope.status = status;
			alert('Properties updated for file : ' + file);
		});
		promise.error(function(data, status) {
			alert('Problem accessing the data : ' + status);
			$scope.status = status;
		});
	};
});

// The controller that populates the indexes drop down
module.controller('IndexesController', function($http, $scope) {
	$scope.index = null;
	$scope.indexes = null;
	$scope.doIndexes = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/indexes');
		var promise = $http.get($scope.url);
		promise.success(function(data, status) {
			$scope.indexes = data;
			$scope.status = status;
		});
	}
	$scope.doIndexes();
});

module.controller('DatabaseController', function($http, $scope) {
	$scope.entities = null;
	$scope.doIndexContexts = function() {
		$scope.url = getServiceUrl('/ikube/service/database/entities');
		$scope.parameters = { 
			class : 'ikube.model.IndexContext',
			startIndex : 0,
			endIndex : 10
		};
		// The configuration for the request to the server
		$scope.config = { params : $scope.parameters };
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.entities = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			alert('Problem accessing the data : ' + status);
			$scope.status = status;
		});
	}
	$scope.doIndexContexts();
});

module.controller('CreateController', function($http, $scope) {
	$scope.entity = null;
	$scope.getEntity = function() {
		$scope.url = getServiceUrl('/ikube/service/database/entity');
		$scope.parameters = { 
			id : '0',
			class : 'ikube.model.IndexContext'
		};
		$scope.config = { params : $scope.parameters };
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.entity = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			alert('Problem accessing the data : ' + status);
			$scope.status = status;
		});
	}
	$scope.getEntity();
	
	$scope.createEntity = function() {
		$scope.url = getServiceUrl('/ikube/service/database/entity/create');
		$scope.headers = { 
			Accept : 'application/json'
		};
		$scope.config = { headers : $scope.headers };
		var promise = $http.post($scope.url, $scope.entity, $scope.config);
		promise.success(function(data, status) {
			$scope.entity = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			alert('Problem creating the entity : ' + status);
			$scope.status = status;
		});
	}
	
});

//The controller that does the search
module.controller('SearcherController', function($scope, $http) {
	
	$scope.fields = [];
	$scope.statistics = null;
	$scope.pageBlock = 10;
	$scope.pagination = null;
	$scope.search = { maxResults : $scope.pageBlock };
	$scope.headers = { headers: { 'Content-Type' : 'application/json' } };
	
	// Go to the web service for the results
	$scope.doSearch = function() {
		$scope.search.maxResults = $scope.pageBlock;
		$scope.url = getServiceUrl('/ikube/service/search/json/complex/sorted/json');
		var promise = $http.post($scope.url, $scope.search);
		promise.success(function(data, status) {
			$scope.search = data;
			$scope.status = status;
			if ($scope.search.searchResults != undefined && $scope.search.searchResults.length > 0) {
				$scope.doPagination();
			}
		});
		promise.error(function(data, status) {
			alert('Data : ' + data + ', status : ' + status);
		});
	};
	// We execute this once to get the search object from the server
	$scope.doSearch();
	
	// Get the fields for the index
	$scope.doFields = function(indexName) {
		$scope.url = getServiceUrl('/ikube/service/monitor/fields');
		$scope.parameters = { indexName : indexName };
		$scope.config = { params : $scope.parameters };
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.fields = [];
			$scope.fields = data;
			$scope.status = status;
			$scope.searchFields = {};
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	
	// Creates the Json pagination array for the next pages in the search
	$scope.doPagination = function() {
		$scope.pagination = [];
		$scope.statistics = $scope.search.searchResults.pop();
		// Exception or no results
		if ($scope.statistics == undefined || $scope.statistics.total == undefined || $scope.statistics.total == null || $scope.statistics.total == 0) {
			$scope.endResult = 0;
			$scope.search.firstResult = 0;
			return;
		}
		// We just started a search and got the first results
		var pages = $scope.statistics.total / $scope.pageBlock;
		// Create one 'page' for each block of results
		for (var i = 0; i < pages && i < 10; i++) {
			var firstResult = i * $scope.pageBlock;
			$scope.pagination[i] = { page : i, firstResult : firstResult };
		};
		// Find the 'to' result being displayed
		var modulo = $scope.statistics.total % $scope.pageBlock;
		$scope.endResult = $scope.search.firstResult + modulo == $scope.statistics.total ? $scope.statistics.total : $scope.search.firstResult + parseInt($scope.pageBlock, 10);
	}
	
	// Set and remove fields from the search object and local scoped objects 
	$scope.setField = function(field, value) {
		$scope.search[field] = value;
	}
	$scope.removeField = function(field, $index) {
		$scope.search[field].splice($index, 1);
	};
	$scope.pushField = function(field, value) {
		$scope.search[field].push(value);
	};
	$scope.pushFieldScope = function(item, value) {
		$scope[item].push(value);
	};
});

//module.controller('TypeaheadController', function($scope, $http, $timeout) {
function TypeaheadController($scope, $http, $timeout) {
	$scope.search = {
		maxResults : 10
	};
	$scope.results = new Array();
	$scope.doSearch = function(uri, searchString) {
		$scope.url = getServiceUrl(uri);
		$scope.search.fragment = true;
		$scope.search.searchStrings = [searchString];
		// alert('Url : ' + $scope.url);
		var promise = $http.post($scope.url, $scope.search);
		promise.success(function(data, status) {
			// Pop the statistics Json off the array
			$scope.search = data;
			$scope.status = status;
			$scope.convertToArray();
			// alert('Array : ' + $scope.results);
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
		$scope.results = new Array();
		// Convert all the data to an array for the auto complete
		$scope.convertToArray = function() {
			$scope.statistics = $scope.search.searchResults.pop();
			// Exception or no results
			if ($scope.statistics == undefined || $scope.statistics.total == undefined || $scope.statistics.total == 0) {
				return;
			}
			// Iterate through the results from the Json data
			for (var key in $scope.search.searchResults) {
				$scope.results.push($scope.search.searchResults['fragment']);
			}
		}
		// Wait for a while for the server to return some data, note 
		// that if the server is still too slow you can add more time to the timeout
		return $timeout(function() {
			return $scope.results;
		}, 250);
	};
}

function writeDate() {
	var d = new Date();
	document.write(d.toLocaleTimeString());
	document.write(' ');
	document.write(d.toLocaleDateString());
}

function printMethods(object) {
	if (object == null) {
		return;
	}
	for (var m in object) {
		print('M : ' + m);
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

//This function will set the width of the iframe dynamically
//so it can take advantage of more space on the screen if it is available
function setIframeWidth(iframe) {
	var PositionXY = {
		Width : 0,
		Height : 0
	};
	var db = document.body;
	var dde = document.documentElement;
	PositionXY.Width = Math.max(db.scrollTop, dde.scrollTop, db.offsetWidth, dde.offsetWidth, db.clientWidth, dde.clientWidth);
	PositionXY.Height = Math.max(db.scrollHeight, dde.scrollHeight, db.offsetHeight, dde.offsetHeight, db.clientHeight, dde.clientHeight);
	// Now take the smaller of the document width and the actual browser width
	PositionXY.Width = Math.min(PositionXY.Width, $(window).width());
	$('#' + iframe).attr('width', PositionXY.Width);
	$('#' + iframe).attr('height', PositionXY.Height);
	return PositionXY;
}