<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<html>
<head>
	<meta http-equiv="Expires" content="-1">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	
	<meta name="Description" content="Ikube Enterprise Search." />
	<meta name="Keywords" content="Michael Couck, Ikube, Enterprise Search, Web Site Search, Database Search, High Volume" />

	<script src="<c:url value="/js/jquery-1.4.4.min.js"/>" ></script>
	<script src="http://www.google-analytics.com/ga.js" type="text/javascript"></script>
	<script src="http://maps.google.com/maps/api/js?sensor=false" type="text/javascript"></script>
	
	<link href="<c:url value="/style/style-white.css"/>" rel="stylesheet" type="text/css" media="screen" />
</head>

<script type="text/javascript">
	try {
		var pageTracker = _gat._getTracker("UA-13044914-4");
		pageTracker._trackPageview();
	} catch (err) {
		document.write('<!-- ' + err + ' -->');
	}
</script>

<script type="text/javascript">
	// The Google map that we will add the points to
	var map = null;
	// The points array for the locations of the results found
	var waypoints = null;
	// The origin of the map
	var origin = null;
	// The destination of the points, i.e. last on on the route
	var destination = null;
</script>

<script type="text/javascript">
	$(document).ready(function() {
		initializeMap();
		$('#button').click(function() {
			results(getServiceUrl());
		});
	});
	
	function initializeMap() {
		var latitude = parseFloat($('#latitude').val());
		var longitude = parseFloat($('#longitude').val());
		var options = {
			center: new google.maps.LatLng(latitude, longitude),
			zoom: 13,
			mapTypeId: google.maps.MapTypeId.ROADMAP
		};
		map = new google.maps.Map(document.getElementById("map_canvas"), options);
		var coordinate = new google.maps.LatLng(latitude, longitude);
		var marker = new google.maps.Marker({
			position: coordinate,
			map : map,
			title : "Coordinate [" + latitude + ", " + longitude + "]",
			icon: "/ikube/image/icon/center_pin.png"
		});
	}
	
	String.prototype.capitalize = function() {
	    return this.charAt(0).toUpperCase() + this.slice(1);
	}
	
	function getServiceUrl() {
		var url = [];
		url.push(window.location.protocol);
		url.push('//');
		url.push(window.location.host);
		url.push('/ikube/service/search/multi/spatial/all');
		return url.join('');
	}
	
	function results(url) {
		$.get(url, 
			{ 
				indexName : 'geospatial', 
				searchStrings : $('#searchStrings').val(),
				fragment : 'true',
				firstResult : '0',
				maxResults : '10',
				distance : '20',
				latitude : $('#latitude').val(),
				longitude : $('#longitude').val()
			}, 
			function(data) {
				$('#results').empty();
				var xmlDom = $(data)
				setPoints(xmlDom);
			}
		);
	}
	
	function setPoints(xmlDom) {
		waypoints = [];
		origin = null;
		destination = null;
		$(xmlDom).find('object').each(function() {
			$(this).find('object').each(function() {
				setPoint($(this));
				$('#results').append('<br>');
			});
		});
		setWaypoints(waypoints);
	}
	
	function setPoint(resultMap) {
		var pointName = null;
		var pointDistance = null;
		var pointLatitude = null;
		var pointLongitude = null;
		
		// Iterate over the properties for this result
		$(resultMap).find('void').each(function() {
			var propertyArray = $(this);
			var name = $(propertyArray).find('string:first').text();
			if (name == null) {
				return;
			}
			var value = $(propertyArray).find('string:last').text();
			
			$.each(['distance', 'name', 'fragment', 'latitude', 'longitude'], function(index, field) {
				if (name == 'name') {
					pointName = value;
				} else if (name == 'distance') {
					pointDistance = value;
				} else if (name == 'latitude') {
					pointLatitude = parseFloat(value);
				} else if (name == 'longitude') {
					pointLongitude = parseFloat(value);
				}
				if (name == field) {
					$('#results').append(name.capitalize()).append(' : ').append(value).append('<br>');
				}
			});
		});
		
		var pointMarker = new google.maps.Marker({
			position: new google.maps.LatLng(pointLatitude, pointLongitude),
			map : map,
			title : 'Name : ' + pointName + ', distance : ' + pointDistance
		});
		
		setWaypoint(waypoints, pointLatitude, pointLongitude);
	}
	
	function setWaypoint(waypoints, pointLatitude, pointLongitude) {
		if (waypoints.length < 8) {
			var pointWaypoint = new google.maps.LatLng(pointLatitude, pointLongitude);
			waypoints.push({ location: pointWaypoint });
			if (origin == null) {
				origin = pointWaypoint;
			}
			destination = pointWaypoint;
		}
	}
	
	function setWaypoints(waypoints) {
		if (waypoints.length > 0) {
			var rendererOptions = { map: map };
			var directionsDisplay = new google.maps.DirectionsRenderer(rendererOptions);
			var request = {
				origin: origin,
				destination: destination,
				waypoints: waypoints,
				travelMode: google.maps.TravelMode.DRIVING,
				unitSystem: google.maps.UnitSystem.METRIC
			};
			
			var directionsService = new google.maps.DirectionsService();
			directionsService.route(request, 
				function(response, status) {
					if (status == google.maps.DirectionsStatus.OK) {
						directionsDisplay.setDirections(response);
					} else {
						alert ('Failed to get directions from Googy, sorry : ' + status);
					}
				}
			);
		}
	}
	
</script>

<body>
	Latitude: <input id="latitude" type="number" value="-33.9693580">
	Longitude: <input id="longitude" type="number" value="18.4622110">
	Search string: <input id="searchStrings" type="text" value="cape town university">

	<input id="button" type="button" value="Go!"><br><br>

	<table>
		<tr>
			<td width="35%"><div id="results" width="100%" height="100%">Results</div></td>
			<td width="65%"><div id="map_canvas" style="width:650px; height:450px; border: solid black 1px;"></div></td>
		</tr>
	</table>

</body>

</html>