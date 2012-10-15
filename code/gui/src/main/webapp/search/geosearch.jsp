<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@page import="ikube.toolkit.SerializationUtilities"%>
<%@page import="java.io.InputStream"%>
<%@page import="ikube.toolkit.FileUtilities"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>

<%
	InputStream inputStream = getServletContext().getResourceAsStream("/WEB-INF/data/results.xml");
	String xml = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
	List<Map<String, String>> results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
%>

<c:set var="indexName" value="${param.indexName != null ? param.indexName : 'geospatial'}" scope="request" />
<c:set var="searchStrings" value="${param.searchStrings != null ? param.searchStrings : 'hotel'}" scope="request" />
<c:set var="latitude" value="${param.latitude != null ? param.latitude : '-33.9693580'}" scope="request" />
<c:set var="longitude" value="${param.longitude != null ? param.longitude : '18.4622110'}" scope="request" />
<c:set var="distance" value="${param.distance != null ? param.distance : '20'}"  scope="request" />
<c:set var="firstResult" value="${param.firstResult != null ? param.firstResult : '0'}" scope="request" />
<c:set var="maxResults" value="${param.maxResults != null ? param.maxResults : '100'}" scope="request" />

<c:set var="total" value="${param.total != null ? param.total : '10'}" scope="request" />
<c:set var="corrections" value="${param.corrections != null ? param.corrections : ''}" scope="request" />
<c:set var="duration" value="${param.duration != null ? param.duration : '0'}" scope="request" />
<c:set var="results" value="${results}" scope="request" />

<c:set var="targetSearchUrl" value="/search/geosearch.jsp" />

<script type="text/javascript">
	window.onload=initialize;
</script>

<form name="geoSearchForm" id="geoSearchForm" action="<c:url value="${targetSearchUrl}"/>">
<input name="targetSearchUrl" type="hidden" value="${targetSearchUrl}">
<table>
	<tr>
		<td valign="top">
			<table style="padding-right: 20px;">
				<tr>
					<td nowrap="nowrap">Index name:</td>
					<td><input id="search-text" type="text" name="indexName" value="<c:out value='${indexName}' />" /></td>
				</tr>
				<tr>
					<td nowrap="nowrap">Search string:</td>
					<td><input id="search-text" type="text" name="searchStrings" value="<c:out value='${searchStrings}' />" /></td>
				</tr>
				<tr>
					<td>Latitude:</td>
					<td><input id="search-text" type="text" name="latitude" value="<c:out value='${latitude}' />" /></td>
				</tr>
				<tr>
					<td>Longitude:</td>
					<td><input id="search-text" type="text" name="longitude" value="<c:out value='${longitude}' />" /></td>
				</tr>
				<tr>
					<td>Distance:</td>
					<td><input id="search-text" type="text" name="distance" value="<c:out value='${distance}' />" /></td>
				</tr>
				<tr>
					<td>First result:</td>
					<td><input id="search-text" type="text" name="firstResult" value="<c:out value='${firstResult}' />" /></td>
				</tr>
				<tr>
					<td>Max results:</td>
					<td><input id="search-text" type="text" name="maxResults" value="<c:out value='${maxResults}' />" /></td>
				</tr>
				<tr>
					<td><input type="submit" id="search-submit" value="Go" /></td>
				</tr>
				<tr>
					<td colspan="2">
						<c:if test="${!empty corrections}">
							<br>
							Did you mean : 
							<a href="#" onclick="JavaScript:submitForm(${corrections})">${corrections}</a><br>
						</c:if>
					</td>
				</tr>
			</table>
		</td>
		<td width="100%">
			<div id="map_canvas" style="width:700px; height:450px; border: solid black 1px;"></div>
		</td>
	</tr>
</table>
</form>

<script type="text/javascript">
	function submitForm(var corrections) {
		var searchStrings = 'searchStrings';
		var geoSearchForm = 'geoSearchForm';
		var oFormObject = document.forms[geoSearchForm];
		oFormObject.elements[searchStrings].value = corrections;
		document.forms[geoSearchForm].submit();
	}
</script>
	
<table>
	<jsp:include page="/search/include.jsp" flush="true" />
</table>

<c:choose>
	<c:when test="${empty total || total == 0}">
		<script type="text/javascript">
  			function initialize() {
    			var options = {
      				center: new google.maps.LatLng(-34.397, 150.644),
      				zoom: 8,
      				mapTypeId: google.maps.MapTypeId.ROADMAP
    			};
    			var map = new google.maps.Map(document.getElementById("map_canvas"), options);
  			}
		</script>
	</c:when>
	<c:otherwise>
		<script type="text/javascript">
  			function initialize() {
				var latitude = parseFloat(${latitude});
				var longitude = parseFloat(${longitude});
  				var options = {
      				center: new google.maps.LatLng(latitude, longitude),
      				zoom: 13,
      				mapTypeId: google.maps.MapTypeId.ROADMAP
    			};
    			var map = new google.maps.Map(document.getElementById("map_canvas"), options);
    			var coordinate = new google.maps.LatLng(latitude, longitude);
    			var marker = new google.maps.Marker({
 			        position: coordinate,
 			        map : map,
 			        title : "Coordinate [<c:out value="${latitude}" />, <c:out value="${longitude}" />]",
 			        icon: "<c:url value="/images/icons/center_pin.png" />"
 			    });
    			<c:forEach var="result" items="${results}">
					coordinate = new google.maps.LatLng(parseFloat(${result['latitude']}),parseFloat(${result['longitude']}));
					marker = new google.maps.Marker({
						position: coordinate,
						map : map,
						title : "<c:out value="${result['name']}" />, distance : <c:out value="${result['distance']}" />"
					});
    			</c:forEach>
    			
    			var wps = [];
    			var org = new google.maps.LatLng(0,0);
    			var dest = new google.maps.LatLng(0,0);
    			/* <c:forEach var="result" varStatus="status" items="${resultsRouted}">
					<c:if test="${status.count <= 8}">
						var latitude = parseFloat(${result['latitude']});
	    				var longitude = parseFloat(${result['longitude']});
    					var waypoint = new google.maps.LatLng(latitude,longitude);
						<c:if test="${status.count == 1}">
							org = waypoint;
						</c:if>
						<c:if test="${status.count == 8}">
							dest = waypoint;
						</c:if>
	 					wps.push({ location: waypoint });
					</c:if>
 				</c:forEach> */
 				
 				var rendererOptions = { map: map };
    			directionsDisplay = new google.maps.DirectionsRenderer(rendererOptions);

    			var request = {
    				origin: org,
    				destination: dest,
    				waypoints: wps,
    				travelMode: google.maps.DirectionsTravelMode.DRIVING
    			};

    			directionsService = new google.maps.DirectionsService();
    			directionsService.route(request, 
    				function(response, status) {
    					if (status == google.maps.DirectionsStatus.OK) {
    						directionsDisplay.setDirections(response);
    					} else {
    						alert ('failed to get directions' + status);
    					}
    				}
    			);
  			}
		</script>
	</c:otherwise>
</c:choose>