<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page errorPage="/WEB-INF/jsp/error.jsp" contentType="text/html" %>

<script type="text/javascript">

module.directive('ngVisible', function() {
    return function(scope, element, attr) {
        scope.$watch(attr.ngVisible, function(visible) {
            element.css('display', visible ? '' : 'none');
        });
    };
});

module.controller('NavigationController', function($scope) {
	$scope.visible = { 
		intro : true,
		clustering : false,
		customisation : false,
		extra : false,
		indexes : false,
		quickstart : false,
		searching : false,
		tutorial : false,
		context : false,
		database : false,
		email : false,
		filesystem : false,
		internet : false
	};
	$scope.navigate = function(show) {
		angular.forEach($scope.visible, function(value, key) {
			$scope.visible[key] = false;
		});
		$scope.visible[show] = true;
	};
});

</script>

<div ng-controller="NavigationController">
	
	<a href="#" ng-click="navigate('intro')">Intro</a>&nbsp;
	<a href="#" ng-click="navigate('quickstart')">Quick start</a>&nbsp;
	<a href="#" ng-click="navigate('indexes')">Indexes</a>&nbsp;
	<a href="#" ng-click="navigate('clustering')">Clustering</a>&nbsp;
	<a href="#" ng-click="navigate('customisation')">Customisation</a>&nbsp;
	<a href="#" ng-click="navigate('searching')">Searching</a>&nbsp;
	<a href="#" ng-click="navigate('tutorial')">Tutorial</a>&nbsp;
	<a href="#" ng-click="navigate('extra')">Extra</a>&nbsp;
	<a href="#" ng-click="navigate('context')">Context</a>&nbsp;
	<a href="#" ng-click="navigate('database')">Database</a>&nbsp;
	<a href="#" ng-click="navigate('email')">Email</a>&nbsp;
	<a href="#" ng-click="navigate('filesystem')">File system</a>&nbsp;
	<a href="#" ng-click="navigate('internet')">Internet</a>
	<br><br>
	
	<div ng-model="visible.intro" ng-visible="visible.intro">
		<jsp:include page="/WEB-INF/jsp/documentation/intro.jsp" />
	</div>
	<div ng-model="visible.quickstart" ng-visible="visible.quickstart">
		<jsp:include page="/WEB-INF/jsp/documentation/quickstart.jsp" />
	</div>
	<div ng-model="visible.indexes" ng-visible="visible.indexes">
		<jsp:include page="/WEB-INF/jsp/documentation/indexes.jsp" />
	</div>
	<div ng-model="visible.clustering" ng-visible="visible.clustering">
		<jsp:include page="/WEB-INF/jsp/documentation/clustering.jsp" />
	</div>
	<div ng-model="visible.customisation" ng-visible="visible.customisation">
		<jsp:include page="/WEB-INF/jsp/documentation/customisation.jsp" />
	</div>
	<div ng-model="visible.searching" ng-visible="visible.searching">
		<jsp:include page="/WEB-INF/jsp/documentation/searching.jsp" />
	</div>
	<div ng-model="visible.tutorial" ng-visible="visible.tutorial">
		<jsp:include page="/WEB-INF/jsp/documentation/tutorial.jsp" />
	</div>
	<div ng-model="visible.extra" ng-visible="visible.extra">
		<jsp:include page="/WEB-INF/jsp/documentation/extra.jsp" />
	</div>
	<div ng-model="visible.context" ng-visible="visible.context">
		<jsp:include page="/WEB-INF/jsp/documentation/configuration/context.jsp" />
	</div>
	<div ng-model="visible.database" ng-visible="visible.database">
		<jsp:include page="/WEB-INF/jsp/documentation/configuration/database.jsp" />
	</div>
	<div ng-model="visible.email" ng-visible="visible.email">
		<jsp:include page="/WEB-INF/jsp/documentation/configuration/email.jsp" />
	</div>
	<div ng-model="visible.filesystem" ng-visible="visible.filesystem">
		<jsp:include page="/WEB-INF/jsp/documentation/configuration/filesystem.jsp" />
	</div>
	<div ng-model="visible.internet" ng-visible="visible.internet">
		<jsp:include page="/WEB-INF/jsp/documentation/configuration/internet.jsp" />
	</div>
	
</div>