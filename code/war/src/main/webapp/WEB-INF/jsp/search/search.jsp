<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!--
	Test cases:
	* String *
	* Numeric *
	* Range *
	* Geospatial (need the sort fix for Lucene) *
	* Done all of the above... ;) *
 -->

<div class="container-fluid" ng-controller="SearcherController">
	<div class="row-fluid">
		<div class="span4">
			<div class="box">
				<div class="tab-header">
					Search form
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
						<!-- <div class="span12"> -->
							<div class="padded">
								<form ng-submit="doSearchAll([searchString])">
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
										focus-me="true"
										ng-model="searchString"
										placeholder="Instant search..."
										typeahead="result for result in doSearch()"
										typeahead-min-length="3" 
										typeahead-wait-ms="500"
										typeahead-on-select="doSearchAll([stripTags(searchString)]);">
									<div class="input search pull-right" ng-show="!!statistics && !!statistics.corrections">
										Did you mean : 
										<a href="#" ng-click="
												searchString = statistics.corrections;
												doSearchAll([statistics.corrections]);">{{statistics.corrections}}
										</a>
									</div>
									<button type="submit" class="button blue" ng-disabled="!searchString" ng-click="doSearchAll([searchString]);">Go</button>
								</div>
								</form>
								
								<form ng-submit="doSearch()">
								<div class="note pull-right"><b>Choose an index to search</b></div>
								<div class="input search">
									<select 
										ng-model="search.indexName"
										ng-model="indexes"
										ng-options="index for index in indexes"
										class="fill-up">
										<option style="display:none" value="">choose...</option>
									</select>
								</div>
								
								<div 
									ng-show="!!search.indexName"
									ng-repeat="field in search.searchFields">
									<div class="prepend-transparent" ng-show="!!search.searchFields[$index]">
      								<span 
      									class="add-on button" 
      									ng-click="search.searchFields[$index] = ''">-</span>
									<input 
										class="input-transparent" 
										type="text" 
										placeholder="{{field}}..."
										ng-model="search.searchStrings[$index]" />
									</div>
								</div>
								
								<div class="input search pull-right" ng-show="!!statistics && !!statistics.corrections && !!search.indexName">
									Corrections : {{statistics.corrections}}
								</div>
								
								<button type="submit" class="button blue" ng-disabled="!search.indexName" ng-click="doSearch();">Go</button>
								</form>

								<div style="width: 10px; height: 60px;"></div>

							</div>
						<!-- </div> -->
					</div>
				</form>
			</div>
		</div>	
		
		<div class="span8">
			<div class="black-box tex">
				<div class="tab-header">Search results</div>
				
				<ul class="recent-comments" ng-show="!!search.coordinate">
					<li class="separator">
						<div class="article-post">
							<div class="user-content">
								<div id="map_canvas" google-map></div>
							</div>
						</div>
					</li>
				</ul>
				
				<ul class="recent-comments" ng-show="!!statistics && statistics.total > 0 && !!search.searchResults && search.searchResults.length > 0">
					<li class="separator">
						<div class="article-post">
							<div class="user-content">
								Results {{search.firstResult}} to {{search.endResult}} of {{statistics.total}}<br> 
								Duration {{statistics.duration}} milliseconds 
								<span ng-show="!!search.searchStrings && search.searchStrings.length != 0">
									Search strings :  
									<span ng-repeat="searchString in search.searchStrings">
										<span ng-show="!!searchString">
											'{{searchString}}'&nbsp;
										</span>
									</span><br> 
									<span ng-show="!!search.searchFields && search.searchFields.length > 0">
										Fields :  
										<span ng-repeat="searchField in search.searchFields">
											<span ng-show="!!search.searchStrings[$index]">
												'{{searchField}}'&nbsp;
											</span>
										</span>
									</span><br>
									<span ng-show="!!statistics.corrections && statistics.corrections.length > 0">
										Corrections :  {{statistics.corrections}}
									</span>
								</span> 
								<br>
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
				
				<ul class="recent-comments" ng-show="!!statistics && !!search.searchResults && search.endResult > 0" ng-repeat="result in search.searchResults">
					<li class="separator">
						<div class="avatar pull-left">
							<img ng-src="{{doFileTypeImage(result.id, result.mimeType)}}" />
						</div>
						<div class="article-post">
							<div class="user-info" ng-show="!!result.id">Id : {{result.id}}</div>
							<div class="user-info" ng-show="!!result.path">Path : {{result.path}}</div>
							<div class="user-info" ng-show="!!result.url">Url : {{result.url}}</div>
							<div class="user-info" ng-show="!!result.score">Score : {{result.score}}</div>
							<div class="user-info" ng-show="!!result.distance">Distance : {{result.distance}}</div>
							<div class="user-info" ng-show="!!result.latitude">Latitude : {{result.latitude}}</div>
							<div class="user-info" ng-show="!!result.longitude">Longitude : {{result.longitude}}</div>
							<div class="user-info" ng-show="!!result.lastmodified">Last modified : {{result.lastmodified}}</div>
							<div class="user-content" ng-show="!!result.fragment" ng-bind-html-unsafe="'Fragment : ' + result.fragment">Fragment :</div>
							<div class="btn-group">
								<button class="button black mini" onClick="enterpriseNotification();"><i class="icon-pencil"></i>Edit</button>
								<button class="button black mini" onClick="enterpriseNotification();"><i class="icon-remove"></i>Delete</button>
								<button class="button black mini" ng-click="search.searchResults.splice($index, 1)"><i class="icon-stop"></i>Hide</button>
							</div>
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