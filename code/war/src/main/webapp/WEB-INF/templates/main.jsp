<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!doctype html>
<html ng-app="ikube">
<head>
	
	<meta charset="utf-8">
	<meta content="IE=edge,chrome=1" http-equiv="X-UA-Compatible">
	
	<meta http-equiv="Expires" content="-1">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	<!-- <meta http-equiv="refresh" content="1800"> -->
	
	<title><tiles:insertAttribute name="title" /></title>
	<link rel="shortcut icon" href="<c:url value="/assets/images/icons/favicon_32x32.ico" />">
	
	<meta name="Description" content="Ikube Enterprise Search." />
	<meta name="Keywords" content="Ikube, Enterprise Search, Web Site Search, Database Search, Network Search, High Volume, Data Mining, Analytics, Machine Learning" />
	
	<link href="<c:url value="/assets/stylesheets/application.css" />" media="screen" rel="stylesheet" type="text/css" />
	
	<!--[if lt IE 9]>
		<script src="<c:url value="/assets/javascripts/html5shiv.js" />" type="text/javascript"></script>
		<script src="<c:url value="/assets/javascripts/excanvas.js" />" type="text/javascript"></script>
		<script src="<c:url value="/assets/javascripts/iefix.js" />" type="text/javascript"></script>
		<link href="<c:url value="/assets/stylesheets/iefix.css" />" media="screen" rel="stylesheet" type="text/css" />
	<![endif]-->
	
	<script src="<c:url value="/assets/javascripts/application.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/docs.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/docs_charts.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/documentation.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/prettify.js" />" type="text/javascript"></script>
	
	<link href="<c:url value="/assets/stylesheets/prettify.css" />" media="screen" rel="stylesheet" type="text/css" />
	
	<script src="https://www.google.com/jsapi" type="text/javascript" ></script>
	<script src="http://maps.google.com/maps/api/js?sensor=false" type="text/javascript"></script>
	<script src="http://www.google-analytics.com/ga.js" type="text/javascript"></script>
	<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.0.7/angular.min.js"></script>
	<!-- <script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.2.0-rc.2/angular.min.js"></script> -->
	<%-- <script src="<c:url value="/assets/javascripts/ui-bootstrap-tpls-0.4.0.js" />" type='text/javascript'></script> --%>
	<script src="<c:url value="/assets/javascripts/ui-bootstrap-tpls-0.6.0.js" />" type='text/javascript'></script>
	
	<!-- Must be after Angular -->
	<script src="<c:url value="/assets/javascripts/services/database-service.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/services/results-builder-service.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/services/config-service.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/ikube.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/directives/directive.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/directives/active-directive.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/directives/textarea-onblur-directive.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/directives/focus-directive.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/searcher-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/actions-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/index-contexts-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/indexes-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/properties-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/servers-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/typeahead-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/active-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/dropdown-controller.js" />" type="text/javascript"></script>
	
</head>

<body onload="JavaScript:track();">
	
	<security:authorize access="isAuthenticated()">
		<tiles:insertAttribute name="menu" />
	</security:authorize>
	<section id="main">
		<security:authorize access="isAuthenticated()">
			<tiles:insertAttribute name="header" />
		</security:authorize>
		<tiles:insertAttribute name="content" />
		<tiles:insertAttribute name="footer" />
	</section>
	
</body>

<script type="text/javascript">
   	prettyPrint();
</script>

</html>