/**
 * This controller gathers the index context data from the server for
 * presentation.
 * 
 * @author Michael Couck
 * @since 10-11-2012
 */
module.controller('IndexContextsController', function($http, $scope, $injector, $timeout, notificationService) {

	$scope.indexContext = undefined;
	$scope.indexContexts = undefined;
	$scope.sortField = 'name';
	$scope.descending = true;
	$scope.viewAll = false;

	$scope.refreshIndexContexts = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/index-contexts');
		$scope.parameters = {
			sortField : $scope.sortField,
			descending : $scope.descending
		};
		$scope.config = {
			params : $scope.parameters
		};
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.indexContexts = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	// Immediately get the data
	$scope.refreshIndexContexts();
	// Refresh the index contexts every so often
	setInterval(function() {
		$scope.refreshIndexContexts();
	}, refreshInterval);

	// This function will publish a start event in the cluster
	$scope.startIndexing = function(indexName) {
		$scope.url = getServiceUrl('/ikube/service/monitor/start');
		// The parameters for the start
		$scope.parameters = {
			indexName : indexName
		};
		// The configuration for the request to the server
		$scope.config = {
			params : $scope.parameters
		};
		// And terminate the indexing for the index
		var promise = $http.post($scope.url, indexName);
		promise.success(function(data, status) {
			$scope.status = status;
			$scope.notification('Start indexing', indexName);
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};

	// This function will delete the index completely on the file system
	$scope.deleteIndex = function(indexName) {
		// alert('Deleting index : ' + indexName);
		$scope.url = getServiceUrl('/ikube/service/monitor/delete-index');
		// The parameters for the delete of the index
		$scope.parameters = {
			indexName : indexName
		};
		// The configuration for the request to the server
		$scope.config = {
			params : $scope.parameters
		};
		// And delete the index
		var promise = $http.post($scope.url, indexName);
		promise.success(function(data, status) {
			$scope.status = status;
			$scope.notification('Delete', indexName);
		});
		promise.error(function(data, status) {
			$scope.status = status;
			// alert('Error sending delete message : ' + status);
		});
	};

	$scope.notification = function(action, indexName) {
		notificationService.notification(action + ' for index \'' + indexName + '\' executed', '/ikube/assets/images/cloud.png', '5');
	};

	$scope.editing = function() {
		return !!$scope.indexContext;
	};

	$scope.cancel = function() {
		$scope.indexContext = null;
	};

	$scope.update = function() {
		// TODO Update the database
	};

	$scope.parent = function() {
		// TODO Get the parent on the page for editing
	};

	// These functions produce the table of indexes
	$scope.dataTable = function dataTable(element) {
		var data = $scope.buildTable();
		var target = $("#" + element);
		target.dataTable({
			"bJQueryUI" : true,
			"bDestroy" : true,
			"sPaginationType" : "full_numbers",
			"sDom" : '<""l>t<"F"fp>',
			"aoColumns" : [ {
				"sTitle" : "Name"
			}, {
				"sTitle" : "Open"
			}, {
				"sTitle" : "Indexing"
			}, {
				"sTitle" : "Documents"
			}, {
				"sTitle" : "Size"
			} ],
			"aaData" : data,
			fnRowCallback : function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
				// Row click
				var row = $(nRow);
				$('td', nRow).attr('nowrap', 'nowrap');
				row.on("click", function() {
					var name = row.find("td:first").text();
					if (name != undefined && $scope.indexContexts != undefined && $scope.indexContexts != null) {
						for ( var i = 0; i < $scope.indexContexts.length; i++) {
							var indexContext = $scope.indexContexts[i];
							if (name == indexContext.name) {
								$scope.indexContext = indexContext;
								$scope.$apply();
							}
						}
					}
				});
			}
		});
	};
	$scope.buildTable = function() {
		var table = [];
		for ( var i = 0; i < $scope.indexContexts.length; i++) {
			var indexContext = $scope.indexContexts[i];
			var row = [];
			row.push(indexContext.name);
			row.push(indexContext.open);
			row.push(indexContext.indexing);
			row.push(indexContext.numDocsForSearchers / 1000000);
			if (!indexContext.snapshot) {
				row.push(0);
			} else {
				row.push(indexContext.snapshot.indexSize / 1000000);
			}
			table.push(row);
		}
		return table;
	};
	var maxRetries = 10;
	$scope.wait = function() {
		$timeout(function() {
			if (maxRetries-- > 0 && $scope.indexContexts != undefined && $scope.indexContexts != null) {
				$scope.dataTable("data-table-json");
			} else {
				$scope.wait();
			}
		}, 250);
	};

	// Call the wait function that will wait for the index contexts to
	// come back from the server before calling the create table function
	// to produce the table
	$scope.wait();

});