<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="container-fluid" ng-controller="IndexContextsController">
	<div class="row-fluid">
		<div class="span6">
			<div class="box" style="position: relative;">
				<div class="tab-header">Indexes&nbsp;&nbsp;&nbsp;
					<i class="icon-refresh" style="cursor: pointer;" ng-click="dataTable('data-table-json');"></i></div>
				<table id="data-table-json" class="table table-striped" style="cursor: pointer;"></table>
			</div>
		</div>
		
		<div class="span6" ng-show="editing();">
			<div class="black-box tex">
				<div class="tab-header">Index : {{indexContext.name}}</div>
				<ul class="recent-comments">
					<li class="separator">
						<div class="article-post">
							<jsp:include page="index.jsp" />
						</div>
					</li>
					<li class="separator" style="text-align: center">
						<a href="#" ng-click="viewAll = !viewAll">View all</a>
					</li>
				</ul>
			</div>
			
			<div class="black-box tex" ng-show="viewAll" ng-repeat="indexContext in indexContexts">
				<div class="tab-header">Index : {{indexContext.name}}</div>
				<ul class="recent-comments">
					<li class="separator">
						<div class="article-post">
							<jsp:include page="index.jsp" />
						</div>
					</li>
				</ul>
			</div>
		</div>
	</div>
</div>