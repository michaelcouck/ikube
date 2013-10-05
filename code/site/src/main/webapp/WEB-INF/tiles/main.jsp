<% response.setHeader("Access-Control-Allow-Origin", "*"); %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>


<html>
<head>
	<meta http-equiv="Expires" content="-1">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	<meta http-equiv="refresh" content="1800">
	
	<title><tiles:insertAttribute name="title" /></title>
	<link rel="shortcut icon" href="<c:url value="/img/icons/favicon.ico" />">
	
	<meta name="Description" content="Ikube Big Data Platform" />
	<meta 
		name="Keywords" 
		content="Ikube, Enterprise Search, Web Site Search, Database Search, High Volume, Analytics, Business Intelligence, Machine Learning" />
	
	<link rel="stylesheet" href="<c:url value="/style/style.css" />" />
	<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
	
	<!-- <script src="https://www.google.com/jsapi" type="text/javascript" ></script> -->
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js" type="text/javascript"></script>
	<script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.9.2/jquery-ui.min.js" type="text/javascript"></script>
	<!-- <script src="//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min.js" type="text/javascript"></script> -->
	<!-- <script src="http://maps.google.com/maps/api/js?sensor=false" type="text/javascript"></script> -->
	<!-- <script src="http://www.google-analytics.com/ga.js" type="text/javascript"></script> -->
	<script src="http://malsup.github.com/jquery.form.js" type="text/javascript" ></script>
	
	<%-- <link rel="stylesheet" type="text/css" href="<c:url value="/js/bootstrap-combined.min.css" />"> --%>
	<script type='text/javascript' src="<c:url value="/js/angular.js" />"></script>
	<script type='text/javascript' src="<c:url value="/js/ui-bootstrap-tpls-0.4.0.js" />"></script>
	
	<!-- Must be after Angular -->
	<script src="<c:url value="/js/ikube.js" />" type="text/javascript"></script>
</head>

<body ng-app="site" onload="track();">
<center>	
	<table width="80%"> 
		<tr>
			<td colspan="2"><tiles:insertAttribute name="header" /></td>
		</tr>
		<tr>
			<td width="20%"><tiles:insertAttribute name="menu" /></td>
			<td width="80%"><tiles:insertAttribute name="content" /></td>
		</tr>
		<tr>
			<td colspan="2"><tiles:insertAttribute name="footer" /></td>
		</tr>
	</table>
</center>
</body>

</html>