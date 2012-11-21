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

function doFocus(elementId) {
	var element = document.getElementById(elementId);
	if (element != null) {
		element.focus();
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
	window.open(href, windowname, 'width=400,height=200,scrollbars=yes');
	return false;
}

/** The Google map that we will add the points to */
var map = null;
/** The points array for the locations of the results found */
var waypoints = null;
/** The origin of the map */
var origin = null;
/** The destination of the points, i.e. last on on the route */
var destination = null;

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

/**
 * This function will capitalize the first letter of a string.
 * 
 * @returns the string with the first letter capital
 */ 
String.prototype.capitalize = function() {
	return this.charAt(0).toUpperCase() + this.slice(1);
};

/**
 * This function sets up the event listener on the search button. 
 */
function setup() {
	$(document).ready(function() {
		populateIndexNames();
		// Make the map invisible initially
		$('#map_canvas').toggle(false);
		// The search button event on click
		$('#button').click(function() {
			search($('#indexName').val());
		});
	});
}

/**
 * This function will set up the map including the center
 * of the map with the single co-ordinate of the origin.
 */
function initializeMap() {
	$('#map_canvas').toggle(true);
	var latitude = parseFloat($('#latitude').val());
	var longitude = parseFloat($('#longitude').val());
	var options = {
		center: new google.maps.LatLng(latitude, longitude),
		zoom: 13,
		mapTypeId: google.maps.MapTypeId.ROADMAP
	};
	map = new google.maps.Map(document.getElementById('map_canvas'), options);
	var coordinate = new google.maps.LatLng(latitude, longitude);
	var marker = new google.maps.Marker({
		position: coordinate,
		map : map,
		title : "Coordinate [" + latitude + ", " + longitude + "]",
		icon: "/ikube/image/icon/center_pin.png"
	});
}

/**
 * This function will go to the monitor service and populate the drop down
 * with all the index names that are defined in the system.
 */
function populateIndexNames() {
	var url = getServiceUrl('/ikube/service/monitor/indexes');
	// TODO implement with Angular
}

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

/**
 * This function will call the rest web service and get the results for the search in xml 
 * format(serialized list of maps) with the last map the statistics for the search.
 * 
 * @param the name of the index
 * @param indexName the name of the index to search
 */
function search(indexName) {
	var monitorUrl = getServiceUrl('/ikube/service/monitor/geospatial');
	// TODO implement with Angular
}

function setResults(xmlDom) {
	origin = null;
	waypoints = [];
	destination = null;
	// The last hash map which is the statistics map
	var statistics = null;
	// This is the iteration over the array list objects in the xml
	if (waypoints.length > 0) {
		setWaypoints(waypoints);
	};
}

function setResult(tbody, resultMap) {
	var pointName = null;
	var pointDistance = null;
	var pointLatitude = null;
	var pointLongitude = null;
	// TODO implement with Angular
	if (pointLatitude != null && pointLongitude != null) {
		var pointMarker = new google.maps.Marker({
			position: new google.maps.LatLng(pointLatitude, pointLongitude),
			map : map,
			title : 'Name : ' + pointName + ', distance : ' + pointDistance
		});
		setWaypoint(waypoints, pointLatitude, pointLongitude);
	}
}

function setWaypoint(waypoints, pointLatitude, pointLongitude) {
	if (waypoints.length >= 8) {
		return;
	}
	var pointWaypoint = new google.maps.LatLng(pointLatitude, pointLongitude);
	waypoints.push({ location: pointWaypoint });
	if (origin == null) {
		origin = pointWaypoint;
	}
	destination = pointWaypoint;
}

function setWaypoints(waypoints) {
	if (waypoints.length == 0) {
		return;
	}
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
				// alert ('Failed to get directions from Googy, sorry : ' + status);
			}
		}
	);
}

