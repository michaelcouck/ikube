<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="container-fluid" ng-controller="TwitterController" ng-init="doConfig('searchTwitterFormConfig');">
	<form>
	<div class="row-fluid">
		<div class="span12">
			<div class="box">
				<div class="tab-header">
					Twitter search...
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
				<div class="row-fluid">
					<div class="padded">
						<div class="pull-right">
							
							<!-- ng-show="!!statistics && statistics.total > 0" -->
							<div>
								<div>
									<div>
										<div class="user-content">
											<b>Found :</b> {{search.firstResult}} to
											{{search.endResult}} of {{statistics.total}} in {{statistics.duration}} milliseconds
										</div>
										<div class="user-content"
											ng-show="!!search.searchStrings && search.searchStrings.length != 0">
											<b>Terms :</b> <span
												ng-repeat="searchString in search.searchStrings track by $index">
												<span ng-show="!!searchString">'{{searchString}}'&nbsp;</span>
											</span>
										</div>
										<div class="user-content"
											ng-show="!!search.searchFields && search.searchFields.length > 0">
											<b>Fields : </b> <span
												ng-repeat="searchField in search.searchFields track by $index">
												<span ng-show="!!searchField">'{{searchField}}'&nbsp;</span>
											</span>
										</div>
										<div class="user-content"
											ng-show="!!search.typeFields && search.typeFields.length > 0">
											<b>Types : </b> <span
												ng-repeat="typeField in searchClone.typeFields track by $index">
												<span ng-show="!!typeField">'{{typeField}}'&nbsp;</span>
											</span>
										</div>
										<div class="user-content"
											ng-show="!!search.coordinate && search.coordinate.latitude != coordinate.latitude">
											<b>Coordinate : </b> [{{search.coordinate.latitude}},
											{{search.coordinate.longitude}}]
										</div>
										<!-- <div class="user-content"
											ng-show="!!statistics.corrections && statistics.corrections.length > 0">
											<b>Corrections : </b> {{statistics.corrections}}
										</div> -->
									</div>
								</div>
							</div>
						</div>
						<div 
							ng-controller="TypeaheadController" 
							ng-init="doConfig('searchTwitterFormConfig');">
							<div class="append-transparent fill-up">
							<input 
								type="text"
								class="input-transparent"
								style="width: 45%;"
								focus-me="true"
								ng-model="searchString"
								placeholder="Find this..."
								typeahead="result for result in doSearch()"
								typeahead-min-length="0" 
								typeahead-wait-ms="500"
								typeahead-on-select="setSearchStrings([stripTags(searchString)]);"
								ng-change="setSearchStrings([stripTags(searchString)]);">
								<button class="add-on button" ng-click="doTwitterSearch(null);">GO</button>
	   	 					</div>
    					</div>
						
						<a class="button mini" style="width: 45%;" onClick="modal('#results-modal');">
							<i class="icon-eye-open"></i>&nbsp;Show results&nbsp;<i class="icon-eye-closed"></i>
						</a>
											
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<div class="row-fluid">
		<div class="span4">
			<div class="box">
				<div class="tab-header">Filters</div>
				<div class="row-fluid">
					<div class="padded">
						<span class="note pull-right"><b>Hours of twitter history ({{search.startHour}})</b></span>
						<div ui-slider min="-168" max="0" ng-model="search.startHour" style="margin-top: 22px;"></div>
						
						<span class="note pull-right" style="margin-top: 22px;">
							<b>Around the point...(optional)</b>
						</span><br>
						<div class="input" style="margin-top: 30px;">
							<a style="width: 70%;" ng-click="doShowMap(!showMap);"><i class="icon-globe"></i>&nbsp;Toggle map</a>
							<a ng-click="doClearCoordinate();"><i class="icon-remove-circle"></i>&nbsp;Clear</a>
						</div>
						
						<div class="input" style="margin-top: 10px; margin-bottom: 10px;" ng-show="showMap">
							<input type="text" ng-model="search.coordinate.latitude" value="{{search.coordinate.latitude}}" style="width: 100%;">
							<input type="text" ng-model="search.coordinate.longitude" value="{{search.coordinate.longitude}}" style="width: 100%;">
							<div id="map_canvas" google-map style="margin-top: 10px; margin-bottom: 10px;"></div>
							<!-- <span class="note pull-right"><b>Coordinates : </b></span> -->
							<!-- <input type="text" ng-model="search.coordinate.latitude" value="{{search.coordinate.latitude}}" style="width: 100%;">
							<input type="text" ng-model="search.coordinate.longitude" value="{{search.coordinate.longitude}}" style="width: 100%;"> -->
						</div>
						
						<span class="note pull-right" style="margin-top: 22px;">
							<b>By language...(optional)</b>
						</span><br>
						<div style="margin-top: 10px;">
							<a href="#" ng-click="search.searchStrings[languageIndex] = ''">All</a>
						</div>
						<div style="margin-top: 3px;" ng-repeat="language in languages">
							<a href="#" ng-click="search.searchStrings[languageIndex] = language">{{language}}</a>
						</div>
					</div>
				</div>
				<!-- <div class="form-actions">
					<a class="button mini red" style="width: 99%; margin-bottom: 8px;" ng-click="doTwitterSearch(null);">
						<i class="icon-thumbs-up"></i>&nbsp;Go&nbsp;<i class="icon-thumbs-down"></i>
					</a>
					<a class="button mini" style="width: 99%;" onClick="modal('#results-modal');">
						<i class="icon-eye-open"></i>&nbsp;Show results&nbsp;<i class="icon-eye-closed"></i>
					</a>
					<div>Similar searches...(clustered data)</div>
				</div> -->
			</div>
		</div>
		
		<div class="span8">
			<div class="box tex">
				<div class="tab-header">Timeline</div>
				<div id="chart_div" style="width: 100%; height: 250px;"></div>
			</div>
		</div>
		
		<div class="span8">
			<div class="box tex">
				<div class="tab-header">Geo tweets</div>
					<div id="geo_chart_div" style="width: 100%; height: 250px;"></div>
			</div>
		</div>
		
		<!-- <div class="span8" ng-show="!!statistics && statistics.total > 0">
			<div class="box tex">
				<div class="tab-header">Statistics</div>
				<div style="padding: 10px;">
					<div class="user-content">
						<b>Results</b> {{search.firstResult}} to {{search.endResult}} of {{statistics.total}} 
					</div>
					<div class="user-content">
						<b>Duration</b> {{statistics.duration}} milliseconds
					</div>
					<div class="user-content" ng-show="!!searchClone.searchStrings && searchClone.searchStrings.length != 0">
						<b>Search strings :</b>
						<span ng-repeat="searchString in searchClone.searchStrings track by $index">
							<span ng-show="!!searchString">'{{searchString}}'&nbsp;</span>
						</span> 
					</div> 
					<div class="user-content" ng-show="!!searchClone.searchFields && searchClone.searchFields.length > 0">
						<b>Fields :  </b>
						<span ng-repeat="searchField in searchClone.searchFields track by $index">
							<span ng-show="!!searchField">'{{searchField}}'&nbsp;</span>
						</span>
					</div>
					<div class="user-content" ng-show="!!searchClone.typeFields && searchClone.typeFields.length > 0">
						<b>Types :  </b>
						<span ng-repeat="typeField in searchClone.typeFields track by $index">
							<span ng-show="!!typeField">'{{typeField}}'&nbsp;</span>
						</span>
					</div>
					<div class="user-content" ng-show="!!searchClone.coordinate && searchClone.coordinate.latitude != coordinate.latitude">
						<b>Coordinate : </b> [{{searchClone.coordinate.latitude}}, {{searchClone.coordinate.longitude}}]
					</div>
					<div class="user-content" ng-show="!!statistics.corrections && statistics.corrections.length > 0">
						<b>Corrections : </b> {{statistics.corrections}}
					</div>
				</div>
			</div>
		</div> -->
	</div>
	
	<!-- <div class="row-fluid">
		<div class="span12" ng-show="!!statistics && statistics.total > 0 && !!search.searchResults && search.searchResults.length > 0">
			<div class="black-box tex">
				<div class="tab-header">Tweets analyzed</div>
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
				
				<ul class="recent-comments" ng-repeat="result in search.searchResults">
					<li class="separator">
						<div class="avatar pull-left">
							<a href="#"><i class="icon-twitter"></i></a>
						</div>
						<div class="article-post">
							<div class="user-content" ng-repeat="(key, value) in result">
								<span ng-show="key != 'fragment' && !!value">{{key}} : {{value}}</span>
							</div>
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
	</div> -->
	
	<jsp:include page="/WEB-INF/jsp/modal/results.jsp" />
	</form>
	
</div>