<%@ taglib prefix="ikube" uri="http://ikube" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">Geo search</span>
			&nbsp;<c:out value="${server.address}" />
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
</table>

<c:choose>
	<c:when test="${empty total || total == 1}">
		<script type="text/javascript">
  			function initialize() {
    			var myOptions = {
      				center: new google.maps.LatLng(-34.397, 150.644),
      				zoom: 8,
      				mapTypeId: google.maps.MapTypeId.ROADMAP
    			};
    			var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
  			}
		</script>
	</c:when>
	<c:otherwise>
		<script type="text/javascript">
  			function initialize() {
    			var myOptions = {
      				center: new google.maps.LatLng(${results[0]['latitude']}, ${results[0]['longitude']}),
      				zoom: 8,
      				mapTypeId: google.maps.MapTypeId.ROADMAP
    			};
    			var resultsMap = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
    			<c:forEach var="result" items="${results}">
	 				<c:out value="${result['latitude']}" />
	 				var point = new google.maps.LatLng(parseFloat(${result['latitude']}),parseFloat(${result['longitude']}));
	 				resultsMap.bounds.extend(point);
	 				var marker = new google.maps.Marker({
	 			        position: point,
	 			        map: resultsMap.map
	 			    });
	 				resultsMap.map.fitBounds(resultsMap.bounds);
    			</c:forEach>
  			}
		</script>
	</c:otherwise>
</c:choose>

<script type="text/javascript">
	window.onload=initialize
</script>

<c:set var="targetSearchUrl" value="/admin/geosearch.html" />
<form name="geoSearchForm" id="geoSearchForm" action="<c:url value="${targetSearchUrl}"/>">
<input name="targetSearchUrl" type="hidden" value="${targetSearchUrl}">
<table width="100%">
	<tr>
		<th>Geo search ${indexName}</th>
	</tr>
	<tr>
		<td>
			<table>
				<tr>
					<td>Index name:</td>
					<td><input id="search-text" type="text" name="indexName" value="<c:out value='${indexName}' />" /></td>
				</tr>
				<tr>
					<td>Search string:</td>
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
					<td>
						<c:if test="${!empty corrections}">
							<br>
							Did you mean : 
							<a href="#" onclick="JavaScript:submitForm(${corrections})">${corrections}</a><br>
						</c:if>
					</td>
				</tr>
			</table>
		</td>
		<td>
			<div id="map_canvas" style="width:300px; height:300px; border: solid black 1px;"></div>
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
	
<table width="100%">
	<jsp:include page="/WEB-INF/jsp/include.jsp" flush="true" />
</table>

<table width="100%">
	<tr>
		<td class="td-content">
			<strong>geo search</strong>&nbsp;
			Search the index and sort the results starting from a co-ordinate, with a maximum distance. 
		</td>
	</tr>
</table>