/**
 * @author Michael Couck
 * @since 24-11-2013
 * 
 * @param $scope
 * @param $http
 * @param $injector
 * @param $timeout
 */
module.controller('AnalyticsController', function($http, $scope, $injector, $timeout) {
	
	$scope.status = 200;
	$scope.showAlgorithmOutput = false;
	$scope.showCorrelationCoefficients = false;
	$scope.showDistributionForInstances = false;
	var textColour = "#aaaaaa";
	
	$scope.headers = ["Cluster", "Cluster probability"];
	$scope.origin = [0, 0];
	$scope.analysis = {
		correlation : true,
		distribution : true,
		distributionForInstances : [$scope.headers, $scope.origin]
	};
	$scope.analyzer;
	$scope.analyzers;
	
	$scope.doAnalysis = function() {
		var url = getServiceUrl('/ikube/service/analyzer/analyze');
		var analysis = angular.copy($scope.analysis);
		analysis.distributionForInstances.splice(0, 1);
		
		analysis.clazz = null;
		analysis.output = null;
		analysis.exception = null;
		analysis.algorithmOutput = null;
		analysis.correlationCoefficients = null;
		analysis.distributionForInstances = null;
		
		var promise = $http.post(url, analysis);
		promise.success(function(data, status) {
			$scope.status = status;
			$scope.analysis = data;
			if ($scope.analysis.distributionForInstances.length == 0) {
				$scope.analysis.distributionForInstances.push($scope.origin);
			}
			$scope.analysis.distributionForInstances.unshift($scope.headers);
			$scope.chart = $scope.doChart($scope.analysis.distributionForInstances);
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	
	$scope.doChart = function(distributionForInstances) {
		var chart = {
			type : "ScatterChart",
			cssStyle : "height:300px; width:100%;",
			data : google.visualization.arrayToDataTable(distributionForInstances),
			options : {
				legend : { position : 'bottom', textStyle : { color : '#aaaaaa', fontSize : 10 } },
				title : "Clustered instances",
				titleTextStyle : { fontSize : 16, color: '#999999' },
				vAxis: { title : 'Probability',  titleTextStyle: { color: textColour, fontSize : 13 } },
				hAxis : { title : 'Clusters',  titleTextStyle : { color: textColour, fontSize : 13 } },
				backgroundColor: { fill : 'transparent' }
			}
		};
		return chart;
	};
	
	$scope.chart = $scope.doChart($scope.analysis.distributionForInstances);
	
	$scope.doTrain = function() {
		var url = getServiceUrl('/ikube/service/analyzer/analyze');
		var promise = $http.post(url, $scope.analysis);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	
	$scope.doAnalyzers = function() {
		var url = getServiceUrl('/ikube/service/monitor/analyzers');
		var promise = $http.get(url);
		promise.success(function(data, status) {
			$scope.status = status;
			$scope.analyzers = data;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	$scope.doAnalyzers();
	
	// a simple model to bind to and send to the server
	$scope.model = {
		name : "",
		comments : ""
	};
	
	// an array of files selected
	$scope.files = [];
	
	// listen for the file selected event
	$scope.$on("fileSelected", function(event, args) {
		$scope.$apply(function() {
			// add the file object to the scope's files collection
			$scope.files.push(args.file);
		});
	});
	
	// the save method
	$scope.save = function() {
		// "/Api/PostStuff"
		var url = getServiceUrl('/ikube/service/analyzer/analyze');
		$http({
			method : 'POST',
			url : url,
			// IMPORTANT!!! You might think this should be set to
			// 'multipart/form-data' but this is not true because when we are sending up files the
			// request needs to include a 'boundary' parameter which identifies the
			// boundary name between parts in this multi-part request and setting the
			// Content-type manually will not set this boundary parameter. For whatever
			// reason, setting the Content-type to 'false' will force the request to
			// automatically populate the headers properly including the boundary parameter.
			headers : {
				'Content-Type' : false
			},
			// This method will allow us to change how the data is sent up to
			// the server for which we'll need to encapsulate the model data in 'FormData'
			transformRequest : function(data) {
				var formData = new FormData();
				// need to convert our json object to a string version of json
				// otherwise the browser will do a 'toString()' on the object which will
				// result in the value '[Object object]' on the server.
				formData.append("model", angular.toJson(data.model));
				// now add all of the assigned files
				for ( var i = 0; i < data.files; i++) {
					// add each file to the form data and iteratively name them
					formData.append("file" + i, data.files[i]);
				}
				return formData;
			},
			// Create an object that contains the model and files which will be
			// transformed in the above transformRequest method
			data : {
				model : $scope.model,
				files : $scope.files
			}
		}).success(function(data, status, headers, config) {
			alert("success!");
		}).error(function(data, status, headers, config) {
			alert("failed!");
		});
	};
	
});