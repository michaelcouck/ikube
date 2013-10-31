<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="container-fluid">
	<div class="row-fluid">
		<div class="span6">
			<div class="row-fluid">
				<div class="span12">
					<div style="margin-bottom: 20px;">
						<div class="big-button-bar">
							<a class="button large" href="#" onclick="enterpriseNotification();"><i class="icon-list-ul"></i><span>Create Index</span></a>
							<a class="button large" href="#" onclick="enterpriseNotification();"><i class="icon-th"></i><span>Create Db</span></a>
							<a class="button large" href="#" onclick="enterpriseNotification();"><i class="icon-user"></i><span>Create User</span></a>
							<a class="button large" href="#" onclick="enterpriseNotification();"><i class="icon-folder-open"></i><span>File system</span></a>
							<a class="button large" href="#" onclick="enterpriseNotification();"><i class="icon-picture"></i><span>Photos</span></a>
							<a class="button large" href="http://www.ikube.be/site"><i class="icon-file"></i><span>Docs</span></a>
						</div>
					</div>
				</div>
			</div>

			<div class="row-fluid" ng-controller="ServersController">
				<div class="span12" ng-repeat="server in servers">
					<table class="table table-striped table-bordered box">
						<thead>
							<tr>
								<th colspan="2">Server : {{server.address}}</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td>Cpu load : </td>
								<td><strong>{{server.averageCpuLoad}}</strong></td>
							</tr>
							<tr>
								<td>Free  memory : </td>
								<td>{{server.freeMemory / 1000}} gig</td>
							</tr>
							<tr>
								<td>Max memory :</td>
								<td>{{server.maxMemory / 1000}} gig</td>
							</tr>
							<tr>
								<td>Total memory : </td>
								<td>{{server.totalMemory / 1000}} gig</td>
							</tr>
							<tr>
								<td>Free disk space : </td>
								<td>{{server.freeDiskSpace}}</td>
							</tr>
							<tr>
								<td>Schedules running : </td>
								<td>{{server.threadsRunning}}</td>
							</tr>
							<tr>
								<td>Cpu throttling : </td>
								<td>{{server.cpuThrottling}}</td>
							</tr>
							<tr>
								<td>Server birth timestamp : </td>
								<td>{{date(server.age)}}</td>
							</tr>
							<tr>
								<td>Available processors : </td>
								<td>{{server.processors}}</td>
							</tr>
							<tr>
								<td>Architecture : </td>
								<td>{{server.architecture}}</td>
							</tr>
						</tbody>
						<tfoot>
							<tr>
								<td colspan="2">
									<div class="clearfix" style="padding: 0 5px;">
										<div class="pull-left">
											<a href="#" class="button blue" ng-click="refreshServer();">Refresh server</a>
											<!-- <a href="#" class="button">Terminate CPU throttling</a> -->
										</div>
									</div>
								</td>
							</tr>
						</tfoot>
					</table>
				</div>
			</div>
			
			<div class="row-fluid" ng-controller="ActionsController" ng-hide="!actions.length">
				<div class="span12" ng-repeat="action in actions">
					<table class="table table-striped table-bordered box">
						<thead>
							<tr>
								<th colspan="2">Server executing action : {{action.server.address}}</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td>Action id and name : </td>
								<td><strong>{{action.id}}:{{action.actionName}}</strong></td>
							</tr>
							<tr>
								<td>Index name : </td>
								<td><strong>{{action.indexName}}</strong></td>
							</tr>
							<tr>
								<td>Current indexable name :</td>
								<td>{{action.indexableName}}</td>
							</tr>
							<tr>
								<td>Documents per minute : </td>
								<td>{{action.snapshot.docsPerMinute}}</td>
							</tr>
							<tr>
								<td>Total documents done : </td>
								<td>{{action.snapshot.numDocsForIndexWriters}}</td>
							</tr>
							<tr>
								<td>Start time : </td>
								<td>{{action.startTime}}</td>
							</tr>
						</tbody>
						<tfoot>
							<tr>
								<td colspan="2">
									<div class="clearfix" style="padding: 0 5px;">
										<div class="pull-left">
											<a href="#" class="button blue" ng-click="terminateAction(action.indexName);">Terminate action</a>
										</div>
									</div>
								</td>
							</tr>
						</tfoot>
					</table>
				</div>
			</div>
		</div>

		<div class="span6">
			<div class="row-fluid">
				<div class="span12">
					<div class="tabbable black-box" style="margin-bottom: 18px;">
						
						<div class="tab-header">
							Cluster statistics 
							<span class="pull-right">
								<span class="options" ng-controller="ServersController">
									<div class="btn-group">
										<a class="dropdown-toggle" data-toggle="dropdown">
											<i class="icon-cog"></i>
										</a>
										<ul class="dropdown-menu black-box-dropdown dropdown-left">
											<li><a href="#" ng-click="refreshServers();">Refresh</a></li>
											<!-- <li><a href="#">Another action</a></li>
											<li><a href="#">Something else here</a></li>
											<li class="divider"></li>
											<li><a href="#">Separated link</a></li> -->
										</ul>
									</div>
								</span>
							</span>
						</div>
						<ul class="nav nav-tabs">
							<li class="active"><a href="#tab1" data-toggle="tab"><i class="icon-globe"></i>Indexing</a></li>
							<li class=""><a href="#tab2" data-toggle="tab"><i class="icon-search"></i>Searching</a></li>
						</ul>
						<div class="tab-content" ng-controller="ServersController">
							<!-- Start: Indexing tab and graph -->
							<div class="tab-pane active" id="tab1">
								<div class="separator">
									<div class="inner-well clearfix">
										<div class="pull-left">Schedules running</div>
										<div class="pull-right">
											<input 
												rel="confirm-check" 
												type="checkbox" 
												id="VKZp4" 
												class="checky" 
												ng-data="server" 
												ng-checked="server.threadsRunning"
												ng-click="toggleThreadsRunning();" />
											<label for="VKZp4" class="checky"><span></span></label>
										</div>
									</div>
									<div class="inner-well clearfix">
										<div class="pull-left">Cpu throttling</div>
										<div class="pull-right">
											<input 
												rel="confirm-check" 
												type="checkbox" 
												id="FNNqp" 
												class="checky" 
												ng-data="server" 
												ng-checked="server.cpuThrottling" 
												ng-click="toggleCpuThrottling();" />
											<label for="FNNqp" class="checky"><span></span></label>
										</div>
									</div>
								</div>
								<div class="separator">
									<div class="inner-well">
										<div indexing style="width: 100%;">The indexing performance graph</div>
									</div>
								</div>
							</div>
							<!-- End: Indexing tab and graph -->
							<!-- Start: Searching tab and graph -->
							<div class="tab-pane" id="tab2">
								<div class="separator">
									<div class="inner-well">
										<div searching style="width: 100%;">Searching performance graph</div>
									</div>
								</div>
							</div>
							<!-- Start: Searching tab and graph -->
						</div>
					</div>
				</div>
			</div>
			
			<div class="row-fluid">
				<div class="span12">
					<div class="black-box tex">
						<div class="tab-header">Recent searches</div>
						<ul class="recent-comments" ng-controller="ServersController">
							<li class="separator" ng-repeat="entity in entities">
								<div class="avatar pull-left">
									<img src="<c:url value="/assets/images/MYP_1376-small.jpg" />" />
								</div>
								<div class="article-post">
									<div class="user-info">Posted by jordan, 3 days ago</div>
									<div class="user-content">Vivamus sed auctor nibh congue,
										ligula vitae tempus pharetra... Vivamus sed auctor nibh
										congue, ligula vitae tempus pharetra... Vivamus sed auctor
										nibh congue, ligula vitae tempus pharetra...</div>
									<div class="btn-group">
										<button class="button black mini">
											<i class="icon-pencil"></i> Edit
										</button>
										<button class="button black mini">
											<i class="icon-remove"></i> Delete
										</button>
										<button class="button black mini">
											<i class="icon-ok"></i> Approve
										</button>
									</div>
								</div>
							</li>
							<li class="separator" style="text-align: center"><a href="#">View all</a></li>
						</ul>
					</div>
				</div>
			</div>
			
		</div>
	</div>
</div>