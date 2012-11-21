<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<% response.setHeader("Access-Control-Allow-Origin", "*"); %>

<!Doctype html>
<html ng-app="ikube">
<head>
	<meta http-equiv="Expires" content="-1">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	
	<meta name="Description" content="Ikube Enterprise Search." />
	<meta name="Keywords" content="Ikube, Enterprise Search, Web Site Search, Database Search, High Volume" />

	<link href="<c:url value="/style/style-white.css"/>" rel="stylesheet" type="text/css" media="screen" />
	<link rel="stylesheet" href="http://code.jquery.com/ui/1.9.1/themes/base/jquery-ui.css" />
    
	<script src="<c:url value="/js/ikube.js"/>" type="text/javascript"></script>
    <script src="http://code.jquery.com/jquery-1.8.2.js" type="text/javascript"></script>
    <script src="http://code.jquery.com/ui/1.9.1/jquery-ui.js" type="text/javascript"></script>
    <script src="http://www.google-analytics.com/ga.js" type="text/javascript"></script>
	<script src="http://maps.google.com/maps/api/js?sensor=false" type="text/javascript"></script>
	<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min.js" type="text/javascript"></script>
	
	<script type="text/javascript">
		var url = 'http://localhost:9080/ikube/service/search/json/multi/all?indexName=geospatial&searchStrings=hotel&fragment=true&firstResult=0&maxResults=10';
		
		var module = angular.module('ikube', ['ngResource']);
		module.controller('SearcherController', function($http, $scope) {
			$http.get(url).success(function(data) {
				$scope.data = data;
			});
		});
		function SearcherJsonController($http, $scope) {
			$http.get(url).success(function(data) {
				// $scope.data = data;
			});
		}
	</script>
</head>

<body>

<div ng-app="ikube" ng-controller="SearcherController">
	<div ng-repeat="datum in data">
		<b>Id</b> : {{datum.id}}<br>
		<b>Score</b> : {{datum.score}}<br> 
		<b>Fragment</b> : {{datum.fragment}}<br>
		<br><br>
	</div>
</div>

<div ng-app="ikube" ng-controller="SearcherJsonController">
	<div ng-repeat="datum in data">
		<b>Id</b> : {{datum.id}}<br>
		<b>Score</b> : {{datum.score}}<br> 
		<b>Fragment</b> : {{datum.fragment}}<br>
		<br><br>
	</div>
</div>
<br><br>

<table>
	<tr>
		<th colspan="2">Advanced Angular search</th>
	</tr>
	
	<tr>
		<td colspan="2" valign="top">
			<table>
				<tr>
					<td>Search text:</td>
					<td><input id="searchStrings" type="text" value="cape town university" width="100%"></td>
				</tr>
				<tr>
					<td align="right"><input id="button" type="button" value="Advanced search"></td>
				</tr>
			</table>
		</td>
	</tr>
</table>
<br><br>

</body>

</html>