<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!doctype html>
<html ng-app="ikube">
<head>
	<meta http-equiv="Expires" content="-1">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	
	<title><tiles:insertAttribute name="title" /></title>
	<link rel="shortcut icon" href="<c:url value="/images/icons/favicon.ico" />">
	
	<meta name="Keywords" content="Ikube, Enterprise Search, Web Site Search, Database Search, High Volume" />
	<meta name="Description" content="Ikube Enterprise Search." />
			
	<link rel="stylesheet" href="<c:url value="/style/style.css" />" />

	<!-- External static resources when offline -->
    <link rel="stylesheet" href="<c:url value="/style/codemirror.css" />" type="text/css" />
	
	<script src="<c:url value="/js/ga.js" />" type="text/javascript"></script>
	<script src="<c:url value="/js/jquery-1.8.2.js" />" type="text/javascript"></script>
	
	<script src="<c:url value="/js/angular.min.js" />" type="text/javascript"></script>
	<script src="<c:url value="/js/angular-ui.js" />" type="text/javascript"></script>
	<script src="<c:url value="/js/codemirror.js" />" type="text/javascript"></script>
    <script src="<c:url value="/js/runmode.js" />" type="text/javascript"></script>
    <script src="<c:url value="/js/jsapi.js" />" type="text/javascript"></script>
    <script src="https://www.google.com/jsapi" type="text/javascript"></script>
    
    <!-- Must be after Angular -->
    <script src="<c:url value="/js/ikube.js" />" type="text/javascript"></script>
</head>

<body onload="JavaScript:track();">

<tiles:insertAttribute name="header" />
<tiles:insertAttribute name="content" />
<tiles:insertAttribute name="footer" />

</body>
</html>