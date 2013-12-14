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
							<div class="input">
								<a class="button green mini" onClick="enterpriseNotification();"><i class="icon-thumbs-up"></i>&nbsp;Positive</a>
								<a class="button red mini" onClick="enterpriseNotification();"><i class="icon-thumbs-down"></i>&nbsp;Negative</a>
							</div><br>
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
									placeholder="Find this..."
									typeahead="result for result in doSearch()"
									typeahead-min-length="3" 
									typeahead-wait-ms="500"
									typeahead-on-select="doSearchAll([stripTags(searchString)]);"
									style="width: 200px;">
								<div class="input search pull-right" ng-show="!!statistics && !!statistics.corrections">
									Did you mean : 
									<a href="#" ng-click="
											searchString = statistics.corrections;
											doSearchAll([statistics.corrections]);">{{statistics.corrections}}
									</a>
								</div>
								<!-- 
									This executes the search when clicked. The typeahead, typeahead-on-select and the enter 
									button will trigger the search. The ng-click attribute on the button will be activated in these cases
									because the button is of type submit. The searchString is from the type ahead scope and needs 
									to be fed into the searcher controller as an array of strings. 
								-->
							</div>
							
							<div class="input">
								<input id="from-date" type="text" class="search" value="From date..." style="width: 200px;">
								<input id="to-date" type="text" class="search" value="To date..." style="width: 200px;">
								<input type="text" class="search" value="Latitude..." style="width: 200px;">
								<input type="text" class="search" value="Longitude..." style="width: 200px;">
							</div>
							
							<button type="submit" class="button blue" ng-disabled="!searchString" ng-click="doSearchAll([searchString]);">Go</button>
							
							</form>
							<div style="width: 10px; height: 60px;"></div>
						</div>
					</div>
				</form>
			</div>
		</div>	
		
		<div class="span8">
			<div class="black-box tex">
				<div class="tab-header">Tweets...</div>
				
				<ul class="recent-comments">
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
									<br>
									Search strings :  
									<span ng-repeat="searchString in search.searchStrings">
										<span ng-show="!!searchString">
											'{{searchString}}'&nbsp;
										</span>
									</span> 
									<span ng-show="!!search.searchFields && search.searchFields.length > 0">
										<br>
										Fields :  
										<span ng-repeat="searchField in search.searchFields">
											<span ng-show="!!search.searchStrings[$index]">
												'{{searchField}}'&nbsp;
											</span>
										</span>
									</span>
									<span ng-show="!!statistics.corrections && statistics.corrections.length > 0">
										<br>
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