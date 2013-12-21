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
					<div 
						style="width: 45%; margin-top: 10px; margin-left: 10px;"
						ng-controller="TypeaheadController" 
						ng-init="doConfig('searchTwitterFormConfig');">
						<div class="append-transparent fill-up">
						<!-- style="width: 45%;" -->
						<input 
							type="text"
							class="input-transparent"
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
   					<div
   						style="width: 45%;" 
   						ng-show="!!statistics && statistics.total > 0 && false">
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
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<div class="row-fluid">
		<div class="span12">
			<div class="box tex">
				<div class="tab-header">Geo tweets</div>
					<div id="geo_chart_div" style="width: 100%; height: 250px;"></div>
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
							<a class="button blue mini" ng-click="doShowMap(!showMap);"><i class="icon-globe"></i>&nbsp;Map</a>
							<a class="button green mini" ng-click="doClearCoordinate();"><i class="icon-remove-circle"></i>&nbsp;Clear</a>
						</div>
						
						<div class="input" style="margin-top: 10px; margin-bottom: 10px;" ng-show="showMap">
							<input type="text" ng-model="search.coordinate.latitude" value="{{search.coordinate.latitude}}" style="width: 100%;">
							<input type="text" ng-model="search.coordinate.longitude" value="{{search.coordinate.longitude}}" style="width: 100%;">
							<div id="map_canvas" google-map style="margin-top: 10px; margin-bottom: 10px;"></div>
						</div>
						
						<span class="note pull-right" style="margin-top: 22px;">
							<b>By language...(optional)</b>
						</span><br>
						<div class="input" style="margin-top: 30px;">
							<a class="button blue mini" ng-click="showLanguages = !showLanguages"><i class="icon-globe"></i>&nbsp;Languages</a>
						</div>
						
						<div ng-show="showLanguages">
							<div style="margin-top: 10px;">
								<a href="#" ng-click="search.searchStrings[languageIndex] = ''">All</a>
							</div>
							<div style="margin-top: 3px;" ng-repeat="language in languages">
								<a href="#" ng-click="search.searchStrings[languageIndex] = language">{{language}}</a>
							</div>
						</div>
						
						<div style="margin-bottom: 45px;"></div>
						
					</div>
				</div>
			</div>
		</div>
		
		<div class="span8">
			<div class="box tex">
				<div class="tab-header">Timeline</div>
				<div id="chart_div" style="width: 100%; height: 250px;"></div>
			</div>
		</div>
	</div>
	
	<jsp:include page="/WEB-INF/jsp/modal/results.jsp" />
	</form>
	
</div>