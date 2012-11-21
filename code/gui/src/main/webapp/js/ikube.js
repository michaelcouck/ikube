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
	// Get the semi-colon seperated list of index names
	$.get(url, function(data) {
		var indexNames = data.split(';');
		var select = $('<select>').attr('id', 'indexName').appendTo('body');
		// Iterate over them and add them to the drop down select
		$(indexNames).each(function(index, value) {
			select.append($('<option>').attr('value', value).text(value));
		});
		$('#indexNames').html(select);
	});
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
	$('#results').empty();
	// Check what type of index it is, if it is geospatial then go to the 
	// geo service otherwise just the normal service
	var monitorUrl = getServiceUrl('/ikube/service/monitor/geospatial');
	$.get(monitorUrl, { indexName : indexName }, function(data) {
		var parameters = {};
		var webServiceUrl = null;
		if ('true' == data) {
			// If this is a geo-spatial search then set up the map
			initializeMap();
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
		} else {
			// Closethe map if open
			$('#map_canvas').toggle(false);
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
		}
		$.get(webServiceUrl, parameters, function(data) {
			var xmlDom = $(data);
			setResults(xmlDom);
		});
	});
	var results = $('#results').html();
	return results;
}

function setResults(xmlDom) {
	waypoints = [];
	origin = null;
	destination = null;
	var tbody = $('#results');
	tbody.empty();
	// The last hash map which is the statistics map
	var statistics = null;
	// This is the iteration over the array list objects in the xml
	$(xmlDom).find('object').each(function() {
		// This is the iteration over the hash maps objects in the xml
		$(this).find('object').each(function() {
			setResult(tbody, $(this));
			addEmptyRow(tbody);
			statistics = this;
		});
	});
	// Set the statistics for the search
	var text = [];
	$(statistics).find('void').each(function() {
		var propertyArray = $(this);
		var name = $(propertyArray).find('string:first').text();
		var value = $(propertyArray).find('string:last').text();
		if ('searchStrings' != name && value != null && '' != value) {
			text.push('<b>' + name + '</b>');
			text.push(' : ');
			text.push(value);
			text.push('<br>');
		}
	});
	$('#statistics').empty();
	$('#statistics').append(text.join(''));
	// TODO Set the paging for the results
	if (waypoints.length > 0) {
		setWaypoints(waypoints);
	};
}

function addEmptyRow(tbody) {
	var emptyTrow = $("<tr>");
	$("<td>").text('&nbsp;').appendTo(emptyTrow);
	$("<td>").text('&nbsp;').appendTo(emptyTrow);
	emptyTrow.appendTo(tbody);
}

function setResult(tbody, resultMap) {
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
		var id = null;
		var value = $(propertyArray).find('string:last').text();
		var fields = ['score', 'distance', 'name', 'fragment', 'latitude', 'longitude', 'path', 'id'];
			$.each(fields, function(index, field) {
			if (name == 'name') {
				pointName = value;
			} else if (name == 'distance') {
				pointDistance = value;
			} else if (name == 'latitude') {
				pointLatitude = parseFloat(value);
			} else if (name == 'longitude') {
				pointLongitude = parseFloat(value);
			} else if (name == 'id') {
				id = value;
			}
			if (name == field) {
				var trow = $("<tr>");
				var tdata = $("<td>").text('<b>' + name.capitalize() + '</b>');
				tdata.appendTo(trow);
				tdata = $("<td>").html(value);
				tdata.appendTo(trow);
				trow.appendTo(tbody);
			}
		});
	});
	
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

function addAutoComplete(inputField) {
	inputField.autocomplete({
		source : getServiceUrl("/ikube/service/auto/complete")
	});
}