<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="container-fluid" ng-controller="SearcherController">
	<div class="row-fluid">
		<div class="span4">
			<div class="box">
				<div class="tab-header">Search form
					<span class="pull-right">
						<span class="options">
							<a href="#"><i class="icon-cog"></i></a>
						</span>
					</span>
				</div>
				<form class="fill-up">
					<div class="row-fluid">
						<div class="span12">
							<div class="padded">
								<form ng-submit="doSearch()">
								<div class="note pull-right"><b>Search all fields in all indexes</b></div>
								<div 
									class="input" 
									ng-controller="TypeaheadController" 
									ng-init="doConfig('searchFormConfig');">
									<input
										id="instant-search"
										name="instant-search" 
										type="text"
										class="search"
										ng-model="searchString"
										focus-me="true"
										placeholder="Instant search..."
										typeahead="result for result in doSearch()"
										typeahead-min-length="3" 
										typeahead-wait-ms="350"
										typeahead-on-select="callService('name', 'parameter');">
									<button type="submit" class="button blue" ng-click="doSearchAll();">Go</button>
								</div>
								</form>
								
								<div class="note pull-right"><b>Choose an index to search</b></div>
								<div class="input search">
									<select 
										ng-model="indexName"
										ng-model="indexes"
										ng-options="index for index in indexes"
										class="fill-up">
										<option style="display:none" value="">choose</option>
									</select>
								</div>
								
								<div 
									class="prepend-transparent"
									ng-show="!!search.searchFields && search.searchFields.length > 0"
									ng-repeat="field in search.searchFields">
									<span class="add-on button">@</span>
									<input class="input-transparent" type="text" placeholder="{{field}}" value="{{field}}" />
								</div>

								<br><br><br><br><br><br><br>

							</div>
						</div>
					</div>
				</form>
			</div>
		</div>	
		
		<div class="span8">
			<div class="black-box tex">
				<div class="tab-header">Search results</div>
				<ul class="recent-comments" ng-show="!!statistics">
					<li class="separator">
						<div class="article-post">
							<div class="user-content">
								Showing results {{search.firstResult}} 
								to {{endResult}} 
								of {{statistics.total}} 
								for {{search.searchStrings}} 
								<span ng-show="!!search.searchFields && search.searchFields.length != 0">in fields {{search.searchFields}},</span> 
								duration {{statistics.duration}} milliseconds<br>
								<div class="btn-group">
									<button class="button mini" ng-repeat="page in pagination" ng-click="setField('firstResult', page.firstResult);doSearch();">{{page.page}}</button>
								</div>
							</div>
						</div>
					</li>
				</ul>
				
				<ul class="recent-comments" ng-show="!!search.searchFields.latitude && !!search.searchFields.longitude">
					<li class="separator">
						<div class="article-post">
							<div class="user-content">
								<div id="map_canvas" google-map style="height: 250px; width: 350px; border : 2px solid black;"></div>
							</div>
						</div>
					</li>
				</ul>
				
				<ul class="recent-comments" ng-show="!!search.searchResults && endResult > 0" ng-repeat="result in search.searchResults">
					<li class="separator">
						<div class="avatar pull-left">
							<img src="<c:url value="/assets/images/MYP_1376-small.jpg" />" />
						</div>
						<div class="article-post">
							<div class="user-info" ng-show="!!result.id">Id : {{result.id}}</div>
							<div class="user-info" ng-show="!!result.path">Path : {{result.path}}</div>
							<div class="user-info" ng-show="!!result.url">Url : {{result.url}}</div>
							<div class="user-info">Score : {{result.score}}</div>
							<div class="user-content" ng-bind-html-unsafe="'Fragment : ' + result.fragment">Fragment :</div>
							<div class="btn-group">
								<button class="button black mini" onClick="enterpriseNotification();"><i class="icon-pencil"></i>Edit</button>
								<button class="button black mini" onClick="enterpriseNotification();"><i class="icon-remove"></i>Delete</button>
								<button class="button black mini" ng-click="search.searchResults.splice($index, 1)"><i class="icon-stop"></i>Hide</button>
							</div>
						</div>
					</li>
				</ul>
			</div>
		</div>
	</div>
</div>