<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!doctype html>
<html ng-app="ikube">
<head>
	<meta http-equiv="Expires" content="-1">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	<meta http-equiv="refresh" content="1800">
	
	<title><tiles:insertAttribute name="title" /></title>
	<link rel="shortcut icon" href="<c:url value="/images/icons/favicon.ico" />">
	
	<meta name="Keywords" content="Ikube, Enterprise Search, Web Site Search, Database Search, High Volume" />
	<meta name="Description" content="Ikube Enterprise Search." />
	
	<link rel="stylesheet" href="<c:url value="/style/ikube.css" />" />
	
	<script src="https://www.google.com/jsapi" type="text/javascript" ></script>
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js" type="text/javascript"></script>
	<script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.9.2/jquery-ui.min.js" type="text/javascript"></script>
	<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min.js" type="text/javascript"></script>
	<script src="http://maps.google.com/maps/api/js?sensor=false" type="text/javascript"></script>
	<script src="http://www.google-analytics.com/ga.js" type="text/javascript"></script>
	<script src="http://malsup.github.com/jquery.form.js" type="text/javascript" ></script>
	
	<!-- Must be after Angular -->
	<script src="<c:url value="/js/ikube.js" />" type="text/javascript"></script>
</head>

<body onload="JavaScript:track();">

	<tiles:insertAttribute name="header" />
	<tiles:insertAttribute name="content" />
	<tiles:insertAttribute name="footer" />

</body>
</html>