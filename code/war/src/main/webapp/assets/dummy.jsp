<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!doctype html>
<html ng-app="sliderDemoApp">
<head>
	
	<meta charset="utf-8">
	<meta content="IE=edge,chrome=1" http-equiv="X-UA-Compatible">
	
	<meta http-equiv="Expires" content="-1">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	
	<title>Dummy</title>
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
	<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.0.7/angular.js"></script>
	
	<!-- <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.1.4/angular.min.js"></script> -->
	<script src="<c:url value="/assets/dummy.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/directives/angular-slider.js" />" type="text/javascript"></script>
	<link href="<c:url value="/assets/stylesheets/angular-slider.css" />" media="screen" rel="stylesheet" type="text/css" />
	
	<!-- <script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.2.0-rc.2/angular.min.js"></script> -->
	<%-- <script src="<c:url value="/assets/javascripts/ui-bootstrap-tpls-0.4.0.js" />" type='text/javascript'></script> --%>
	<script src="<c:url value="/assets/javascripts/ui-bootstrap-tpls-0.6.0.js" />" type='text/javascript'></script>
	
	<!-- jQuery -->
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
	<script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/jquery-ui.min.js"></script>
		
	<!-- Must be after Angular -->
	<script src="<c:url value="/assets/javascripts/ng-google-chart.js" />" type="text/javascript"></script>
	<!-- Services that are injected into controllers. -->
	<script src="<c:url value="/assets/javascripts/services/database-service.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/services/results-builder-service.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/services/config-service.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/services/notification-service.js" />" type="text/javascript"></script>
	<!-- Base logic and functions -->
	<script src="<c:url value="/assets/javascripts/ikube.js" />" type="text/javascript"></script>
	<!-- Directives -->
	<script src="<c:url value="/assets/javascripts/directives/google-map-directive.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/directives/index-graph-directive.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/directives/search-graph-directive.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/directives/chart-directive.js" />" type="text/javascript"></script>
	
	<script src="<c:url value="/assets/javascripts/directives/directive.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/directives/active-directive.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/directives/textarea-onblur-directive.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/directives/focus-directive.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/directives/file-upload-directive.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/directives/angular-slider.js" />" type="text/javascript"></script>
	<!-- Controllers -->
	<script src="<c:url value="/assets/javascripts/controllers/searcher-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/actions-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/index-contexts-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/indexes-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/properties-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/servers-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/typeahead-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/active-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/analytics-controller.js" />" type="text/javascript"></script>
	<script src="<c:url value="/assets/javascripts/controllers/twitter-controller.js" />" type="text/javascript"></script>
	
	<link rel="stylesheet" href="http://angular-ui.github.io/ui-slider/bower_components/jquery-ui/themes/smoothness/jquery-ui.css">
	<script type="text/javascript" src="http://angular-ui.github.io/ui-slider/bower_components/jquery/jquery.min.js"></script>
  	<script type="text/javascript" src="http://angular-ui.github.io/ui-slider/bower_components/jquery-ui/ui/minified/jquery-ui.min.js"></script>
  	<script type="text/javascript" src="http://angular-ui.github.io/ui-slider/bower_components/angular/angular.min.js"></script>	
	<script type="text/javascript" src="http://angular-ui.github.io/ui-slider/src/slider.js"></script>
	
	<script>
		var app = angular.module('sliderDemoApp', ['ui.slider']);
		app.factory('colorpicker', function () {
			function hexFromRGB(r, g, b) {
				var hex = [r.toString(16), g.toString(16), b.toString(16)];
				angular.forEach(hex, function(value, key) {
					if (value.length === 1) hex[key] = "0" + value;
				});
				return hex.join('').toUpperCase();
			}
			return {
				refreshSwatch: function (r, g, b) {
					var color = '#' + hexFromRGB(r, g, b);
					angular.element('#swatch').css('background-color', color);
				}
			};
		});
		app.controller('sliderDemoCtrl', function($scope, colorpicker) {
			function refreshSwatch (ev, ui) {
				var red = $scope.colorpicker.red,
					green = $scope.colorpicker.green,
					blue = $scope.colorpicker.blue;
				colorpicker.refreshSwatch(red, green, blue);
			}

			$scope.demoVals = {
				sliderExample3:     14,
				sliderExample4:     14,
				sliderExample5:     50,
				sliderExample8:     0.34,
				sliderExample9:     [-0.52, 0.54],
				sliderExample10:     -0.37
			};

			$scope.colorpicker = {
				red: 255,
				green: 140,
				blue: 60,
				options: {
					orientation: 'horizontal',
					min: 0,
					max: 255,
					range: 'min',
					change: refreshSwatch,
					slide: refreshSwatch
				}
			};
		});
	</script>
	
</head>

<body>

<div ng-controller="sliderDemoCtrl">

	Hello world!<br>
	
	<h1>AngularUI Slider demo</h1>
	<div class="sliderExample">
		<strong>Step slider</strong>
		<div ui-slider min="0" max="50" ng-model="demoVals.sliderExample1"></div>
		<input type="text" ng-model="demoVals.sliderExample1" />
	</div>
	
</div>
	
</body>

</html>