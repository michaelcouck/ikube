<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<html>
<head>
<script src="<c:url value="/js/jquery-1.4.4.min.js"/>"></script>
</head>

<script type="text/javascript">
	$(document).ready(function() {
		$('#button').click(function() {
			var url = buildUri();
			alert('Click : ' + url);
			$('#iframe').attr("src", url);
			document.getElementById('iframe').contentDocument.location.reload(true);
		});
	});
	
	function buildUri() {
		var uri = [];

		uri.push(window.location.protocol);
		uri.push("//");
		uri.push(window.location.host);

		uri.push("/ikube/results.jsp?");
		uri.push("indexName=geospatial&");

		uri.push("searchStrings=");
		var searchString = $("#searchStrings").val();
		searchString = encodeURIComponent(searchString);
		uri.push(searchString);
		uri.push("&");

		uri.push("fragment=true&");
		uri.push("firstResult=0&");
		uri.push("maxResults=10&");
		uri.push("distance=10&");

		var latitude = $("#latitude").val();
		uri.push("latitude=");
		uri.push(latitude);
		uri.push("&");

		var longitude = $("#longitude").val();
		uri.push("longitude=");
		uri.push(longitude);

		// alert("Uri : " + uri.join(""));

		return uri.join("");
	}
</script>

<body>

	<input id="longitude" type="number" value="18.4622110">
	<input id="latitude" type="number" value="-33.9693580">
	<input id="searchStrings" type="text" value="cape town university">

	<input id="button" type="button"><br>

	<iframe id="iframe" width="100%" height="100%">Results</div>

</body>

</html>

