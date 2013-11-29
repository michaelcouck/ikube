<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<% response.setHeader("Access-Control-Allow-Origin", "*"); %>
<% response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept"); %>
<% response.setHeader("Access-Control-Allow-Methods", "GET, POST"); %>
<% response.setHeader("Access-Control-Allow-Credentials", "true"); %>

<div id="maincontent" ng-controller="SearcherController" ng-init="doSearch(0);">
	<h2>Search Ikube</h2>
	Results '{{searchParameters.firstResult}} 
	to {{endResult}} 
	of {{statistics.total}}' 
	for search '{{searchParameters.searchStrings}}', 
	duration : {{statistics.duration}}
	
	<br><br>
	<span ng-repeat="page in pagination">
		<a style="font-color : {{page.active}}" href="#" 
			ng-click="doSearch(page.firstResult);">{{page.page}}</a>
	</span>
	<br><br>
	
	<div ng-repeat="datum in data">
		<span ng-hide="!datum.id"><b>Url</b> : <a href="{{datum.id}}">{{datum.id}}</a></span><br>
		<span ng-hide="!datum.path"><b>Path</b> : <a href="{{datum.path}}">{{datum.path}}</a></span><br>
		<b>Score</b> : {{datum.score}}<br>
		<span ng-hide="!datum.title"><b>Title</b> : {{datum.title}}</span><br>
		<b>Index</b> : {{datum.index}}<br>
		<b>Fragment</b> : <span ng-bind-html-unsafe="datum.fragment"></span> 
		<br><br>
	</div>
	<br>
	
	<span ng-repeat="page in pagination">
		<a style="font-color : {{page.active}}" href="#" 
			ng-click="doSearch(page.firstResult);">{{page.page}}</a>
	</span>
			
</div>