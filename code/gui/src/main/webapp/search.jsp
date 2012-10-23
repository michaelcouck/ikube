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

	<script src="<c:url value="/js/jquery-1.4.4.min.js"/>" type="text/javascript"></script>
	<script src="http://www.google-analytics.com/ga.js" type="text/javascript"></script>
	<script src="http://maps.google.com/maps/api/js?sensor=false" type="text/javascript"></script>
	<script src="<c:url value="/js/ikube.js"/>" type="text/javascript"></script>
	
	<link href="<c:url value="/style/style-white.css"/>" rel="stylesheet" type="text/css" media="screen" />
</head>

<script type="text/javascript">track();</script>
<script type="text/javascript">setup();</script>

<body>
<center>

<!--  border="solid 0px black" -->
<table>
	<tr>
		<th colspan="5">Advanced search</th>
	</tr>
	<tr>
		<td>Collection</td>
		<td colspan="4"><div id="indexNames"><!-- Populated from jQuery --></div></td>
	</tr>
	<tr>
		<td>Latitude:</td>
		<td colspan="4"><input id="latitude" type="number" value="-33.9693580"></td>
	</tr>
	<tr>
		<td>Longitude:</td>
		<td colspan="4"><input id="longitude" type="number" value="18.4622110"></td>
	</tr>
	<tr>
		<td>All of these words:</td>
		<td colspan="4"><input id="allWords" type="text" value="cape town university" width="100%"></td>
	</tr>
	<tr>
		<td>This exact word or phrase:</td>
		<td><input id="exactPhrase" type="text" value=""></td>
	</tr>
	<tr>
		<td>One or more of these words:</td>
		<td><input id="oneOrMore" type="text" value=""></td>
	</tr>
	
	<tr>
		<td>None of these words:</td>
		<td><input id="noneOfTheseWords" type="text" value=""></td>
	</tr>
	
	<tr>
		<td colspan="5" align="right"><input id="button" type="button" value="Advanced search"></td>
	</tr>
</table>
<br>

<a href="#" onclick="JavaScript:toggleMap()">Show/hide map</a><br>
<div id="map_canvas" style="width:500px; height:400px; border: solid black 1px; align: left; text-align: left;" align="left"></div>
<br><br>

<div id="results" style="width: 65%; text-align: left;"><!-- Results to be apended here by jQuery --></div>

</center>
</body>

</html>