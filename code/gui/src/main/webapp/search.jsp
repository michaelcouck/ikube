<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<% response.setHeader("Access-Control-Allow-Origin", "*"); %>

<html>
<head>
	<meta http-equiv="Expires" content="-1">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	
	<meta name="Description" content="Ikube Enterprise Search." />
	<meta name="Keywords" content="Michael Couck, Ikube, Enterprise Search, Web Site Search, Database Search, High Volume" />

	<link href="<c:url value="/style/style-white.css"/>" rel="stylesheet" type="text/css" media="screen" />
	<link rel="stylesheet" href="http://code.jquery.com/ui/1.9.1/themes/base/jquery-ui.css" />
    
	<script src="<c:url value="/js/ikube.js"/>" type="text/javascript"></script>
    <script src="http://code.jquery.com/jquery-1.8.2.js"></script>
    <script src="http://code.jquery.com/ui/1.9.1/jquery-ui.js"></script>
    <script src="http://www.google-analytics.com/ga.js" type="text/javascript"></script>
	<script src="http://maps.google.com/maps/api/js?sensor=false" type="text/javascript"></script>
	
</head>

<script type="text/javascript">track();</script>
<script type="text/javascript">setup();</script>

<body>

<script>
$(document).ready(function() {
	addAutoComplete($("#allWords"));
	addAutoComplete($("#exactPhrase"));
	addAutoComplete($("#oneOrMore"));
	addAutoComplete($("#noneOfTheseWords"));
});
</script>

<table>
	<tr>
		<th colspan="3">Advanced search</th>
	</tr>
	<tr>
		<td colspan="2"></td>
		<td rowspan="2"><div id="map_canvas" style="width:500px; height:400px; border: solid black 1px; align: left; text-align: left;" align="left"></div></td>
	</tr>
	
	<tr>
		<td colspan="2" valign="top">
			<table>
				<tr>
					<td>Collection</td>
					<td><div id="indexNames"><!-- Populated from jQuery --></div></td>
				</tr>
				<tr>
					<td>All of these words:</td>
					<td><input id="allWords" type="text" value="cape town university" width="100%"></td>
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
					<td>Latitude:</td>
					<td><input id="latitude" type="number" value="-33.9693580"></td>
				</tr>
				<tr>
					<td>Longitude:</td>
					<td><input id="longitude" type="number" value="18.4622110"></td>
				</tr>
				<tr>
					<td colspan="2" align="right"><input id="button" type="button" value="Advanced search"></td>
				</tr>
			</table>
		</td>
	</tr>
</table>
<br><br>

<div id="statistics" ><!-- Filled by jQuery with the results totals and duration --></div>

<table id="results">
	<tbody>
	</tbody>
</table>

</body>

</html>