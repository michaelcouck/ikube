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
	<link rel="shortcut icon" href="<c:url value="/img/icons/favicon.ico" />">
	
	<meta name="Description" content="Ikube Enterprise Search." />
	<meta name="Keywords" content="Ikube, Enterprise Search, Web Site Search, Database Search, Network Search, High Volume, Data Mining, Analytics, Machine Learning" />
	
	<link rel="stylesheet" href="<c:url value="/css/bootstrap.min.css" />" />
	<link rel="stylesheet" href="<c:url value="/css/bootstrap-responsive.min.css" />" />
	<link rel="stylesheet" href="<c:url value="/css/font-awesome.min.css" />" />
	<link rel="stylesheet" href="<c:url value="/css/darkstrap.min.css" />" />
	
	<script src="https://www.google.com/jsapi" type="text/javascript" ></script>
	<script src="http://maps.google.com/maps/api/js?sensor=false" type="text/javascript"></script>
	<script src="http://www.google-analytics.com/ga.js" type="text/javascript"></script>
	<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.0.7/angular.min.js"></script>
	<script src="<c:url value="/js/bootstrap.min.js" />" type="text/javascript" ></script>
	<script src="<c:url value="/js/ui-bootstrap-tpls-0.4.0.js" />" type='text/javascript'></script>
	
	<%-- <link rel="stylesheet" href="<c:url value="/js/bootstrap-combined.min.css" />"> --%>
	<%-- <script src="<c:url value="/js/jquery-ui.js" />" type='text/javascript'></script>
	<script src="<c:url value="/js/jquery-1.9.1.js" />" type='text/javascript'></script> --%>
	
	<!-- Must be after Angular -->
	<script src="<c:url value="/js/ikube.js" />" type="text/javascript"></script>
</head>

<body data-spy="scroll" data-target=".navbar" onload="JavaScript:track();">

<table style="margin-top: 55px; padding: 8px; margin-left: 5px;">
	<tr>
		<td colspan="2" valign="top" align="left">
			<tiles:insertAttribute name="header" />
		</td>
	</tr>
	<tr>
		<td valign="top">
			<tiles:insertAttribute name="menu" />
		</td>
		<td valign="top">
			<tiles:insertAttribute name="content" />
		</td>
	</tr>
	<tr>
		<td colspan="2" valign="top">
			<tiles:insertAttribute name="footer" />
		</td>
	</tr>
</table>

</body>
</html>