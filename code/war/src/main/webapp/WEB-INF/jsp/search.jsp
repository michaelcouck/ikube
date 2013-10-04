<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<% response.setHeader("Access-Control-Allow-Origin", "*"); %>

<table ng-controller="SearcherController" class="table" style="margin-top: 55px;">
	<form ng-submit="doSearch()">
	<tr class="odd" nowrap="nowrap" valign="bottom">
		<td><b>Collection:</b></td>
		<td>
			<select ng-controller="IndexesController" ng-model="indexName" ng-change="doFields()">
				<option ng-repeat="index in indexes" value="{{index}}">{{index}}</option>
			</select>
		</td>
	</tr>
	<tr class="even" nowrap="nowrap" valign="bottom">
		<td><b>All of these words:</b></td>
		<td><input id="bla" name="bla"></td>
	</tr>
	
	<tr class="even" nowrap="nowrap" valign="bottom">
		<td colspan="2">
			<button type="submit" class="btn" id="submit" name="submit">Go!</button>
		</td>
	</tr>
	</form>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<span ng-show="search.indexName != 'undefined'" >
		<tr nowrap="nowrap" valign="bottom">
			<td colspan="2">
				{{search}}
				Showing results '{{search.firstResult}} 
				to {{endResult}} 
				of {{statistics.total}}' 
				for search '{{search.searchStrings}}', 
				corrections : {{statistics.corrections}}, 
				duration : {{statistics.duration}}</td>
		</tr>
	</span>
	
	<tr nowrap="nowrap" valign="bottom">
		<td colspan="2" nowrap="nowrap">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" ng-click="
					doFirstResult(page.firstResult);
					doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr ng-repeat="datum in data" ng-class-odd="'odd'" ng-class-even="'even'">
		<td colspan="2">
			<span ng-hide="!datum.id"><b>Identifier</b> : {{datum.id}}<br></span> 
			<b>Score</b> : {{datum.score}}<br>
			<b>Fragment</b> : <span ng-bind-html-unsafe="datum.fragment"></span><br>
			<br>
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<td colspan="2" nowrap="nowrap">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" ng-click="doFirstResult(page.firstResult);doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
</table>