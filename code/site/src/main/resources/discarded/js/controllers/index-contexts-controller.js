/** This controller gathers the index context data from the server for presentation. */
module.controller('IndexContextsController', function($http, $scope, $timeout) {
	
	$scope.indexContext = undefined;
	$scope.indexContexts = [];
	$scope.sortField = 'name';
	$scope.descending = true;
	$scope.viewAll = false;
	
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
	};
	// Immediately refresh the data
	$scope.refreshIndexContexts();
	// Refresh the index contexts every so often
	setInterval(function() {
		$scope.refreshIndexContexts();
	}, refreshInterval);
	

	// This function will publish a start event in the cluster
	$scope.startIndexing = function(indexName) {
		alert('Starting indexing : ' + indexName);
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
	};
	
	// This function will delete the index completely on the file system
	$scope.deleteIndex = function(indexName) {
		alert('Deleting index : ' + indexName);
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
	};
	
	$scope.editing = function() {
		return $scope.indexContext != undefined && $scope.indexContext != null;
	};
	
	$scope.cancel = function() {
		$scope.indexContext = null;
	};
	
	$scope.dataTable = function dataTable(element, data) {
		var target = $("#" + element);
		target.dataTable( {
			"bJQueryUI" : true,
			"sPaginationType" : "full_numbers",
			"sDom" : '<""l>t<"F"fp>',
			"aoColumns": [
			    { "sTitle" : "Name" },
			    { "sTitle" : "Open" },
			    { "sTitle" : "Documents" },
			    { "sTitle" : "Size" }
	        ],
	        "aaData" : data,
			 fnRowCallback : function(nRow) {
				// Row click
				var row = $(nRow);
				$('td', nRow).attr('nowrap','nowrap');
				row.on("click", function() {
					var name = row.find("td:first").text();
					for (var i = 0; i < $scope.indexContexts.length; i++) {
						var indexContext = $scope.indexContexts[i];
						if (name == indexContext.name) {
							$scope.indexContext = indexContext;
							$scope.$apply();
						}
					}
				});
			}
	    } );
	};
	
	$scope.modal = function modal(element) {
		return $(element).modal();
	};
	
	return $timeout(function() {
		// We need to create an array of arrays for the data table
		// because it seems that the population using pure Json data
		// does not work!
		var table = [];
		for (var i = 0; i < $scope.indexContexts.length; i++) {
			var indexContext = $scope.indexContexts[i];
			var row = [];
			row.push(indexContext.name);
			row.push(indexContext.open);
			row.push(indexContext.numDocsForSearchers);
			row.push(indexContext.snapshot.indexSize);
			table.push(row);
		}
		$scope.dataTable("data-table-json", table);
		return $scope.indexContexts;
	}, 250);
	
});