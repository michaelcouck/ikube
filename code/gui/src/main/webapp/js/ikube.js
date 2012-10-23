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
		toggleMap();
		initializeMap();
		populateIndexNames();
		$('#button').click(function() {
			var indexName = $('#indexName').val();
			search(indexName);
		});
	});
}

function toggleMap() {
	$('#map_canvas').toggle();
	initializeMap();
}

/**
 * This function will set up the map including the center
 * of the map with the single co-ordinate of the origin.
 */
function initializeMap() {
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
	$.get(url, function(data) {
		var select = [];
		var indexNames = data.split(';');
		var select = $('<select>').attr('id', 'indexName').appendTo('body');
		$(indexNames).each(function(index, value) {
			select.append($('<option>').attr('value', value).text(value));
		});
		$('#indexNames').html(select);
	});
}

/**
 * This function builds the url to the rest search service
 * 
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
 * @param indexName the name of the index to search
 */
function search(indexName) {
	initializeMap();
	$('#results').empty();
	// Check what type of index it is, if it is geospatial then go to the 
	// geo service otherwise just the normal service
	var monitorUrl = getServiceUrl('/ikube/service/monitor/geospatial');
	$.get(monitorUrl, { indexName : indexName }, function(data) {
		var parameters = null;
		var webServiceUrl = null;
		if ('true' != data) {
			webServiceUrl = getServiceUrl('/ikube/service/search/multi/advanced/all');
			var searchStrings = [];
			searchStrings.push($('#allWords').val());
			searchStrings.push($('#exactPhrase').val());
			searchStrings.push($('#oneOrMore').val());
			searchStrings.push($('#noneOfTheseWords').val());
			parameters = { 
				indexName : indexName, 
				searchStrings : searchStrings.join(';'),
				searchFields: 'contents', 
				fragment : 'true',
				firstResult : '0',
				maxResults : '10'
			};
		} else {
			webServiceUrl = getServiceUrl('/ikube/service/search/multi/spatial/all');
			parameters = { 
				indexName : indexName, 
				searchStrings : $('#allWords').val(),
				fragment : 'true',
				firstResult : '0',
				maxResults : '10',
				distance : '20',
				latitude : $('#latitude').val(),
				longitude : $('#longitude').val()
			};
		}
		$.get(webServiceUrl, parameters, function(data) {
			var xmlDom = $(data);
			setResults(xmlDom);
		});
	});
}

function setResults(xmlDom) {
	waypoints = [];
	origin = null;
	destination = null;
	$(xmlDom).find('object').each(function() {
		$(this).find('object').each(function() {
			setResult($(this));
		});
	});
	if (waypoints.length > 0) {
		setWaypoints(waypoints);
	}
}

function setResult(resultMap) {
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
		var fields = ['distance', 'name', 'fragment', 'latitude', 'longitude'];
		$.each(fields, function(index, field) {
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
	$('#results').append('<br>');
	
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
			alert ('Failed to get directions from Googy, sorry : ' + status);
		}
	}
	);
}