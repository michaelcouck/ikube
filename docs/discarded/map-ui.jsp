<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<% response.setHeader("Access-Control-Allow-Origin", "*"); %>

<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Expires" content="-1">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	
	<link rel="shortcut icon" href="<c:url value="/img/icons/favicon.ico" />">
	
	<meta name="Keywords" content="Ikube, Enterprise Search, Web Site Search, Database Search, High Volume" />
	<meta name="Description" content="Ikube Enterprise Search." />
	
	<script src="http://maps.googleapis.com/maps/api/js?sensor=false&language=en"></script>
	<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min.js"></script>
	<script src="<c:url value="/js/angular-ui.js" />"></script>
	
</head>

<script type="text/javascript">
	//Add the requried module 'angular-ui' as a dependency
	var module = angular.module('maptesting', [ 'ui' ]);

	function MapCtrl($scope) {
		var ll = new google.maps.LatLng(-33.9693580, 18.4622110);
		$scope.mapOptions = {
			center : ll,
			zoom : 13,
			mapTypeId : google.maps.MapTypeId.ROADMAP
		};

		//Markers should be added after map is loaded
		$scope.onMapIdle = function() {
			var marker = new google.maps.Marker({
				map : $scope.myMap,
				position : ll
			});
			$scope.myMarkers = [ marker ];
		};

		$scope.markerClicked = function(m) {
			window.alert("clicked");
		};

	}
</script>

<body>

	<div ng-app='maptesting'>
		<div ng-controller="MapCtrl">
			<div 
				id="map_canvas" 
				ui-map="myMap"
				style="height: 300px; width: 400px; border: 2px solid #777777; margin: 3px; border: 1px solid"
				ui-options="mapOptions" >
			</div>

			<!--In addition to creating the markers on the map, 
				div elements with existing google.maps.Marker object should 
				be created to hook up with events -->
			<!-- <div 
				ng-repeat="marker in myMarkers"
				ui-map-marker="myMarkers[$index]"
				ui-event="{'map-click': 'markerClicked(marker)'}">
			</div> -->
		</div>
	</div>

</body>

</html>