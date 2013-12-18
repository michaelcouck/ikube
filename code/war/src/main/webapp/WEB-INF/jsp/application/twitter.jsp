<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="container-fluid" ng-controller="TwitterController" ng-init="doConfig('searchTwitterFormConfig');">
	<div class="row-fluid">
		<div class="span4">
			<div class="box">
				<div class="tab-header">
					Twitter analytics
					&nbsp;&nbsp;
					<img 
						ng-show="!status" 
						alt="Loading spinner" 
						src="<c:url value="/assets/images/loading.gif" />" 
						height="16px" 
						width="16px" >
					<span class="pull-right">
						<span class="options">
							<a href="#"><i class="icon-cog"></i></a>
						</span>
					</span>
				</div>
				<form class="fill-up">
					<div class="row-fluid">
						<div class="padded">
							<form>
							<span class="note pull-right"><b>Find term in Twitter time line...(optional)</b></span>
							<div 
								class="input" 
								ng-controller="TypeaheadController" 
								ng-init="doConfig('searchTwitterFormConfig');"
								style="padding-bottom: 10px;">
								<input
									type="text"
									class="search fill-up"
									focus-me="true"
									ng-model="searchString"
									placeholder="Find this..."
									typeahead="result for result in doSearch()"
									typeahead-min-length="0" 
									typeahead-wait-ms="500"
									typeahead-on-select="setSearchStrings([stripTags(searchString)]);"
									ng-change="setSearchStrings([stripTags(searchString)]);">
							</div>
							
							<span class="note pull-right"><b>Hours of twitter history ({{search.startHour}})</b></span>
							<div ui-slider min="-168" max="0" ng-model="search.startHour" style="margin-top: 22px;"></div>
							
							<span class="note pull-right" style="margin-top: 22px;"><b>Around the point...(optional)</b></span><br>
							<div class="input" style="margin-top: 30px;">
								<a class="button mini" style="width: 49%;" ng-click="doShowMap(!showMap);"><i class="icon-globe"></i>&nbsp;Map</a>
								<a class="button mini" style="width: 49%;" ng-click="doClearCoordinate();"><i class="icon-remove-circle"></i>&nbsp;Clear</a>
							</div>
							
							<div class="input fill-up" style="margin-top: 10px;" ng-show="showMap">
								<div id="map_canvas" google-map style="margin-top: 10px; margin-bottom: 10px;"></div>
								<span class="note pull-right"><b>Coordinates : </b></span>
								<input class="search" ng-model="search.coordinate.latitude" value="{{search.coordinate.latitude}}">
								<input class="search" ng-model="search.coordinate.longitude" value="{{search.coordinate.longitude}}">
							</div>
							
							<span class="note pull-right" style="margin-top: 22px;"><b>Select a language...(optional)</b></span><br>
							
							<select
								class="fill-up"
								style="height: 26px; margin-bottom: 15px;"
								ng-model="language" 
								ng-options="language for language in languages">
								<option style="display:none" value="">Language...</option>
							</select>
							
							<span class="note"><b>Positive or negative sentiment?</b></span>
							<div class="input" style="margin-top: 8px;">
								<a class="button mini" style="width: 49%;" ng-click="doTwitterSearch('positive');"><i class="icon-thumbs-up"></i>&nbsp;Positive</a>
								<a class="button mini" style="width: 49%;" ng-click="doTwitterSearch('negative');"><i class="icon-thumbs-down"></i>&nbsp;Negative</a><br><br>
							</div>
							<a class="button mini" style="width: 99%;" ng-click="doTwitterSearch(null);"><i class="icon-thumbs-up"></i>&nbsp;Both&nbsp;<i class="icon-thumbs-down"></i></a>
							
							</form>
							
							<br><br>
							<div>Similar searches...(clustered data)</div>
							
							<div style="width: 10px; height: 60px;"></div>
						</div>
					</div>
				</form>
			</div>
		</div>	
		
		<div class="span8">
			<div class="box tex">
				<div class="tab-header">Tweets...</div>
				<div id="chart_div" style="width: 100%; height: 446px;"></div>
			</div>
		</div>
	</div>
	
	Show results : {{showResults && !!statistics && statistics.total > 0 && !!search.searchResults && search.searchResults.length > 0}}
	
	<div class="row-fluid">
		<div class="span12" ng-show="showResults && !!statistics && statistics.total > 0 && !!search.searchResults && search.searchResults.length > 0">
			<div class="black-box tex">
				<div class="tab-header">Tweets analyzed</div>
				<ul class="recent-comments">
					<li class="separator">
						<div class="article-post">
							<div class="user-content">
								Results {{search.firstResult}} to {{search.endResult}} of {{statistics.total}}<br> 
								Duration {{statistics.duration}} milliseconds
								<div class="user-content" ng-show="!!searchClone.searchStrings && searchClone.searchStrings.length != 0">
									Search strings :
									<span ng-repeat="searchString in searchClone.searchStrings track by $index">
										<span ng-show="!!searchString">'{{searchString}}'&nbsp;</span>
									</span> 
								</div> 
								<div class="user-content" ng-show="!!searchClone.searchFields && searchClone.searchFields.length > 0">
									Fields :  
									<span ng-repeat="searchField in searchClone.searchFields track by $index">
										<span ng-show="!!searchField">'{{searchField}}'&nbsp;</span>
									</span>
								</div>
								<div class="user-content" ng-show="!!searchClone.typeFields && searchClone.typeFields.length > 0">
									Types :  
									<span ng-repeat="typeField in searchClone.typeFields track by $index">
										<span ng-show="!!typeField">'{{typeField}}'&nbsp;</span>
									</span>
								</div>
								<div class="user-content" ng-show="!!searchClone.coordinate && searchClone.coordinate.latitude != coordinate.latitude">
									Coordinate :  [{{searchClone.coordinate.latitude}}, {{searchClone.coordinate.longitude}}]
								</div>
								<div class="user-content" ng-show="!!statistics.corrections && statistics.corrections.length > 0">
									Corrections :  {{statistics.corrections}}
								</div>
								<div class="btn-group">
									<button 
										class="button mini" 
										ng-repeat="page in pagination" 
										ng-click="doPagedSearch(page.firstResult);">{{page.page}}</button>
								</div>
							</div>
						</div>
					</li>
				</ul>
				
				<ul class="recent-comments" ng-repeat="result in search.searchResults">
					<li class="separator">
						<div class="avatar pull-left">
							<a href="#"><i class="icon-twitter"></i></a>
						</div>
						<div class="article-post">
							<div class="user-content" ng-repeat="(key, value) in result">
								<span ng-show="key != 'fragment' && !!value">{{key}} : {{value}}</span>
							</div>
							<!-- <div class="user-info">From user : {{result['from-user']}}</div>
							<div class="user-info" ng-show="!!result.score">Score : {{result.score}}</div>
							<div class="user-info" ng-show="!!result.distance">Distance : {{result.distance}}</div>
							<div class="user-info" ng-show="!!result.latitude">Latitude : {{result.latitude}}</div>
							<div class="user-info" ng-show="!!result.longitude">Longitude : {{result.longitude}}</div> -->
							<div class="user-content" ng-show="!!result.fragment" ng-bind-html-unsafe="'Fragment : ' + result.fragment"></div>
						</div>
					</li>
				</ul>
				
				<ul class="recent-comments" ng-show="!!statistics && statistics.total > 0 && !!search.searchResults && search.searchResults.length > 0">
					<li class="separator">
						<div class="article-post">
							<div class="user-content">
								<div class="btn-group">
									<button 
										class="button mini" 
										ng-repeat="page in pagination" 
										ng-click="doPagedSearch(page.firstResult);">{{page.page}}</button>
								</div>
							</div>
						</div>
					</li>
				</ul>
			</div>
		</div>
	</div>
</div>