<!--This jsp file is replaced with the jQuery search.jsp, delete when ready. -->

<%@ page import="java.net.URL" %>
<%@ page import="ikube.IConstants" %>
<%@ page import="ikube.toolkit.SerializationUtilities" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.apache.commons.httpclient.HttpClient" %>
<%@ page import="org.apache.commons.httpclient.methods.GetMethod" %>
<%@ page import="org.apache.commons.httpclient.NameValuePair" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<head>
	<meta http-equiv="Expires" content="-1">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	
	<meta name="Description" content="Ikube Enterprise Search." />
	<meta name="Keywords" content="Michael Couck, Ikube, Enterprise Search, Document Search, Web Site Search, Database Search, High Volume" />

	<script src="<c:url value="/js/jquery-1.4.4.min.js"/>" ></script>
	<script src="<c:url value="/js/jquery.query.js"/>" ></script>
	<script src="http://maps.google.com/maps/api/js?sensor=false" type="text/javascript"></script>
	
	<link href="<c:url value="/style/style-white.css"/>" rel="stylesheet" type="text/css" media="screen" />
	
</head>

<!-- Do the search and put the results in the session. --> 
<% search(request, session); %>

<!-- Set up all the variables and parameters from the request -->
<c:set var="firstResult" value="${param.firstResult}" />
<c:set var="maxResults" value="${param.maxResults}" />
<c:set var="searchStrings" value="${param.searchStrings}" />
<c:set var="latitude" value="${!empty param.latitude ? param.latitude : -34.397}" />
<c:set var="longitude" value="${!empty param.longitude ? param.longitude : 150.644}" />

<c:set var="total" value="${statistics['total']}" />
<c:set var="duration" value="${statistics['duration']}" />

<c:set var="toResults" value="${total < firstResult + maxResults ? firstResult + (total % 10) : firstResult + maxResults}" />

<script type="text/javascript">
	// The Google map that we will add the points to
	var map = null;
	// The points array for the locations of the results found
	var waypoints = [];
	
	// Init the map and anything else that needs to be set up
	$(document).ready(function () {
		// Set up the map and the origin
		initialize();
		// Add the points to the map
		points();
		// Add the directions to the map
		directions();
	});
</script>

<c:if test="${!empty searchStrings && !empty total}">
	From : <c:out value='${firstResult + 1}' />,
	to : <c:out value='${toResults}' />,
	total : <c:out value='${total}' />,
	for '<c:out value='${searchStrings}' />',
	took <c:out value='${duration}' /> ms
</c:if>
<br><br>

<table>
	<tr>
		<td>
			<c:forEach var="result" items="${results}">
				<c:forEach var="entry" items="${result}">
					<c:out value="${entry.key}" /> : <c:out value="${entry.value}" escapeXml="false" />
					<br>
				</c:forEach>
				<br>
			</c:forEach>
		</td>
		<td><div id="map_canvas" style="width:650px; height:450px; border: solid black 1px;"></div></td>
	</tr>
</table>

<script type="text/javascript">
	function initialize() {
		var latitude = parseFloat(${latitude});
		var longitude = parseFloat(${longitude});
		alert('Latitude : ' + latitude + ', ' + longitude);
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
	
</script>

<script type="text/javascript">
	function points() {
		// Iterate over the points and add them to the map
		var pointName = null;
		var pointDistance = null;
		var pointLatitude = null;
		var pointLongitude = null;
		var pointCoordinate = null;
		var pointMarker = null;
		<c:forEach var="result" items="${results}">
			pointName = '${result['name']}';
			pointDistance = '${result['distance']}';
			pointLatitude = parseFloat(${result['latitude']});
			pointLongitude = parseFloat(${result['longitude']});
			pointCoordinate = new google.maps.LatLng(pointLatitude, pointLongitude);
			alert('Map : ' + map + ', ' + pointCoordinate + ', ' + pointName + ', ' + pointDistance);
			pointMarker = new google.maps.Marker({
				position: pointCoordinate,
				map : map,
				title : 'Name : ' + pointName + ', distance : ' + pointDistance
			});
		</c:forEach>
	}
		
	function directions() {
		var org = new google.maps.LatLng(0,0);
		var dest = new google.maps.LatLng(0,0);
		var pointLatitude = null;
		var pointLongitude = null;
		var pointWaypoint = null;
		// Iterate over the route points and add them to the map directions object
		<c:forEach var="result" varStatus="status" items="${resultsRouted}">
			<c:if test="${status.count <= 8}">
				pointLatitude = parseFloat(${result['latitude']});
				pointLongitude = parseFloat(${result['longitude']});
				pointWaypoint = new google.maps.LatLng(pointLatitude, pointLongitude);
				<c:if test="${status.count == 1}">org = pointWaypoint;</c:if>
				<c:if test="${status.count == 8}">dest = pointWaypoint;</c:if>
				waypoints.push({ location: pointWaypoint });
			</c:if>
		</c:forEach>
		if (waypoints.length > 0) {
			var rendererOptions = { map: map };
			var directionsDisplay = new google.maps.DirectionsRenderer(rendererOptions);
			var request = {
				origin: org,
				destination: dest,
				waypoints: waypoints,
				travelMode: google.maps.DirectionsTravelMode.DRIVING
			};
			var directionsService = new google.maps.DirectionsService();
			directionsService.route(request, 
				function(response, status) {
					alert('Response : ' + response + ', status : ' + status);
					if (status == google.maps.DirectionsStatus.OK) {
						directionsDisplay.setDirections(response);
					} else {
						alert ('Failed to get directions : ' + status);
					}
				}
			);
		}
	}
</script>

<!-- Common methods below here. -->
<%!
	// Go to the rest wervice and get the results for this query. The results will be a 
	// serialized list of maps. We will then deserialize the xml from the service and re-create 
	// the list of maps that is easier to pressent on the page, rather than parsing the xml.
	void search(final HttpServletRequest request, final HttpSession session) {
		try {
			// Build the url to the rest service for the query
			StringBuilder queryString = new StringBuilder();
			queryString.append("/ikube/service/search/multi/spatial/all?");
			queryString.append(request.getQueryString());
			String url = new URL("http", request.getServerName(), request.getLocalPort(), queryString.toString()).toString(); 
			
			// Create the client to access the resource
			GetMethod getMethod = new GetMethod(url);
			HttpClient httpClient = new HttpClient();
			int result = httpClient.executeMethod(getMethod);
			String xml = getMethod.getResponseBodyAsString();
			
			// Convert the results xml into a list of maps that we can display
			ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(xml);
			if (results.size() > 0) {
				HashMap<String, String> statistics = results.remove(results.size() - 1);
				session.setAttribute(IConstants.STATISTICS, statistics);
			}
			session.setAttribute(IConstants.RESULTS, results);
			
			// Calculate the route between the points
			ArrayList<HashMap<String, String>> resultsRouted = routed(results);
			session.setAttribute(IConstants.RESULTS_ROUTED, resultsRouted);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//	This method is a btute force calculation of the shortes route between the points. The more 
	//	efficient option would be to use an neural network, but this falls outside the scope of this simple
	//	search results example page.
	ArrayList<HashMap<String, String>> routed(final ArrayList<HashMap<String, String>> results) {
		// These are the points sorted according to the shortest distance to visit them all
		ArrayList<HashMap<String, String>> resultsRouted = new ArrayList<HashMap<String, String>>();
		if (results.size() > 1) {
			HashMap<String, String> topResult = results.get(0);
			resultsRouted.add(topResult);
			do {
				// Recursively find the closest result to the top result and add it to the sorted array
				HashMap<String, String> bestResult = null;
				double bestDistance = Long.MAX_VALUE;
				for (HashMap<String, String> nextResult : results) {
					if (topResult == nextResult || resultsRouted.contains(nextResult)) {
						continue;
					}
					double lat1 = Double.parseDouble(topResult.get(IConstants.LATITUDE));
					double lon1 = Double.parseDouble(topResult.get(IConstants.LONGITUDE));
					double lat2 = Double.parseDouble(nextResult.get(IConstants.LATITUDE));
					double lon2 = Double.parseDouble(nextResult.get(IConstants.LONGITUDE));
					double nextDistance = distance(lat1, lon1, lat2, lon2, 'K');
					if (nextDistance < bestDistance) {
						bestDistance = nextDistance;
						bestResult = nextResult;
					}
				}
				resultsRouted.add(bestResult);
				topResult = bestResult;
			} while (resultsRouted.size() < results.size());
		}
		return resultsRouted;
	}

	/* :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: : */
	/* :: This routine calculates the distance between two points (given the : */
	/* :: latitude/longitude of those points). It is being used to calculate : */
	/* :: the distance between two ZIP Codes or Postal Codes using our : */
	/* :: ZIPCodeWorld(TM) and PostalCodeWorld(TM) products. : */
	/* :: : */
	/* :: Definitions: : */
	/* :: South latitudes are negative, east longitudes are positive : */
	/* :: : */
	/* :: Passed to function: : */
	/* :: lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees) : */
	/* :: lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees) : */
	/* :: unit = the unit you desire for results : */
	/* :: where: 'M' is statute miles : */
	/* :: 'K' is kilometers (default) : */
	/* :: 'N' is nautical miles : */
	/* :: United States ZIP Code/ Canadian Postal Code databases with latitude & : */
	/* :: longitude are available at http://www.zipcodeworld.com : */
	/* :: : */
	/* :: For enquiries, please contact sales@zipcodeworld.com : */
	/* :: : */
	/* :: Official Web site: http://www.zipcodeworld.com : */
	/* :: : */
	/* :: Hexa Software Development Center © All Rights Reserved 2004 : */
	/* :: : */
	/* :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'K') {
			dist = dist * 1.609344;
		} else if (unit == 'N') {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts decimal degrees to radians : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}
	
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}
%>