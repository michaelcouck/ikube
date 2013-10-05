<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<% response.setHeader("Access-Control-Allow-Origin", "*"); %>

<!DOCTYPE html>
<html ng-app="angular-google-maps">
<head>
	<meta http-equiv="Expires" content="-1">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	
	<link rel="shortcut icon" href="<c:url value="/img/icons/favicon.ico" />">
	
	<meta name="Keywords" content="Ikube, Enterprise Search, Web Site Search, Database Search, High Volume" />
	<meta name="Description" content="Ikube Enterprise Search." />
			
    <script src="http://maps.googleapis.com/maps/api/js?sensor=false&language=en"></script>
	<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min.js"></script>
	<script src="<c:url value="/js/angular-google-maps.js" />"></script>
    
</head>

<script type="text/javascript">
	
	var module = angular.module("angular-google-maps", [ "google-maps" ]);
	
	module.controller('MapController', function($scope) {
		// The configuration for the map and the markers
		$scope.configuration = {
			centerProperty : { lat : 51.10600101811778, lng : 17.025117874145508 },
			zoomProperty : 13,
			markersProperty : [ { latitude : 51.1047951799623, longitude : 17.02278971672058 } ],
			clickedLatitudeProperty : null,
			clickedLongitudeProperty : null,
		};
		
		$scope.doMarkers = function() {
			
		}
		
		angular.extend($scope, $scope.configuration);
	});

</script>

<body ng-controller="MapController">

	<div 
		class="google-map" 
		center="centerProperty" 
		zoom="zoomProperty"
		markers="markersProperty" 
		latitude="clickedLatitudeProperty"
		longitude="clickedLongitudeProperty" 
		mark-click="false"
		draggable="true" 
		style="height: 340px; width: 550px; border : 1px solid black;"></div>

</body>

</html>