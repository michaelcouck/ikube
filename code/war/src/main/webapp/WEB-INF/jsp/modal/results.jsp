<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<div id="results-modal" class="black-box modal hide fade">
	<div class="modal-header tab-header">
		<button type="button" class="close" data-dismiss="modal">&times;</button>
		<span>Analytics results</span>
	</div>
	
	<div class="modal-body separator">
		<h4>Results from the query</h4>
		
		<div class="padded">
		<div ng-show="!!statistics && statistics.total > 0 && !!search.searchResults && search.searchResults.length > 0">
			<div class="btn-group">
				<button 
					class="button mini" 
					ng-repeat="page in pagination" 
					ng-click="doPagedSearch(page.firstResult);">{{page.page}}</button>
			</div>
		</div>
		
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
	
	<div class="modal-footer">
		<div class="inner-well">
			<a class="button mini rounded light-gray" data-dismiss="modal">Close</a>
		</div>
	</div>
</div>