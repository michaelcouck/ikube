<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<div id="maincontent" ng-controller="SearcherController" ng-init="doSearch(0);">
	<h2>Search Ikube</h2>
	Results '{{searchParameters.firstResult}} 
	to {{endResult}} 
	of {{statistics.total}}' 
	for search '{{searchParameters.searchStrings}}', 
	duration : {{statistics.duration}}
	
	<!-- TODO : If the corrections are not empty then do a 'did you mean row' -->
	<!-- corrections : {{statistics.corrections}}, -->
	<br><br>
	<span ng-repeat="page in pagination">
		<a style="font-color : {{page.active}}" href="#" 
			ng-click="doSearch(page.firstResult);">{{page.page}}</a>
	</span>
	<br><br>
	
	<div ng-repeat="datum in data">
		<span ng-hide="!datum.id">
			<b>Url</b> : <a href="{{datum.id}}">{{datum.id}}</a><br>
		</span> 
		<b>Score</b> : {{datum.score}}<br>
		<span><b>Title</b> : {{datum.title}}<br></span>
		<span><b>Index</b> : {{datum.index}}<br></span>
		<span><b>Fragment</b> : {{datum.fragment}}<br></span>
		<!-- <span><b>Datum</b> : {{datum}}<br></span> -->
		<br>
	</div>
	<br>
	
	<span ng-repeat="page in pagination">
		<a style="font-color : {{page.active}}" href="#" 
			ng-click="doSearch(page.firstResult);">{{page.page}}</a>
	</span>
			
</div>