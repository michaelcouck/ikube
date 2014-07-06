<%--suppress ALL --%>
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
				<div style="margin-bottom: 20px;">
					<div class="big-button-bar" ng-controller="NotificationController">
						<a class="button large" href="#" ng-click="enterpriseNotification();"><i class="icon-list-ul"></i><span>Create Index</span></a>
						<a class="button large" href="#" ng-click="enterpriseNotification();"><i class="icon-th"></i><span>Create Db</span></a>
						<a class="button large" href="#" ng-click="enterpriseNotification();"><i class="icon-user"></i><span>Create User</span></a>
						<a class="button large" href="#" ng-click="enterpriseNotification();"><i class="icon-folder-open"></i><span>File system</span></a>
						<a class="button large" href="#" ng-click="enterpriseNotification();"><i class="icon-picture"></i><span>Photos</span></a>
						<a class="button large" href="http://www.ikube.be/site"><i class="icon-file"></i><span>Docs</span></a>
					</div>
				</div>
			</div>

			<!-- Servers -->
			<div class="nav-menu box" ng-controller="ServersController">
				<ul class="nav nav-list">
					<li class="active">
						<a href="#"><i class="icon-cloud"></i>Servers</a>
					</li>
					<li ng-repeat="server in servers">
						<a href="#" ng-click="server.show = !server.show">
							<i class="icon-globe"></i>{{server.address}}
							<span class="pull-right badge" title="Cpu load">{{server.averageCpuLoad}}</span>
						</a>
						<ul class="nav nav-list" style="margin-left: 10px; padding: 5px;" ng-show="server.show">
							<li><i class="icon-th-list"></i>Free memory : {{server.freeMemory / 1000}} gig</li>
							<li><i class="icon-th"></i>Max memory : {{server.maxMemory / 1000}} gig</li>
							<li><i class="icon-th-large"></i>Total memory : {{server.totalMemory / 1000}} gig</li>
							<li><i class="icon-hdd"></i>Free disk space : {{server.freeDiskSpace}}</li>
							<li><i class="icon-play-circle"></i>Jobs running : {{server.threadsRunning}}</li>
							<li><i class="icon-play-circle"></i>Cpu throttling : {{server.cpuThrottling}}</li>
							<li><i class="icon-time"></i>Server timestamp : {{date(server.age)}}</li>
							<li><i class="icon-stop"></i>Available processors : {{server.processors}}</li>
							<li><i class="icon-cogs"></i>Architecture : {{server.architecture}}</li>
						</ul>
					</li>
				</ul>
			</div>
			
			<!-- Actions -->
			<div class="nav-menu box" ng-controller="ActionsController" ng-hide="!actions.length">
				<ul class="nav nav-list">
					<li class="active">
						<a href="#"><i class="icon-briefcase"></i>Jobs</a>
					</li>
					<li ng-repeat="action in actions" style="overflow-wrap: normal; flex-wrap: nowrap; text-wrap: none; word-wrap: normal; ">
                        <a href="#" title="Action, index, indexable being executed" ng-click="action.show = !action.show">
                            <i class="icon-play-circle"></i>
                            <%--class="trim-info"--%>
                            <span>{{action.actionName}} : {{action.indexName}} : {{action.indexableName}} : {{action.server.ip}}</span>
                            <span
                                class="pull-right badge"
                                title="Documents per minute"
                                ng-show="!!action.snapshot.docsPerMinute">{{action.snapshot.docsPerMinute}}</span>
                        </a>

                        <ul class="nav nav-list" style="margin-left: 10px; padding: 5px;" ng-show="action.show">
							<li><i class="icon-globe"></i>Server : {{action.server.address}}</li>
							<li><i class="icon-time"></i>Start timestamp : {{action.startTime}}</li>
                            <li><i class="icon-time"></i>End timestamp : {{action.endTime}}</li>
							<li><i class="icon-th-list"></i>Index writer documents : {{action.snapshot.numDocsForIndexWriters}}</li>
							<li>
								<ul>
									<li>
										<i class="icon-stop"></i>
										<a href="#" ng-click="terminateAction(action.indexName);" style="padding: 3px; font-weight: bold;">Terminate action</a>
									</li>
								</ul>
							</li>
						</ul>
					</li>
				</ul>
			</div>
		</div>

		<div class="span6">
			<div class="row-fluid">
				<div class="span12">
					<div class="tabbable black-box" style="margin-bottom: 18px;">
						
						<div class="tab-header">
							Cluster statistics
                            <div class="btn-group options pull-right" ng-controller="ServersController">
                                <a class="dropdown-toggle" data-toggle="dropdown">
                                    <i class="icon-cog"></i>
                                </a>
                                <ul class="dropdown dropdown-menu black-box-dropdown dropdown-left" style="font-size: small;">
                                    <li><a href="#" ng-click="refreshServers();"><i class="icon-refresh">&nbsp;</i>Refresh</a></li>
                                </ul>
                            </div>
							<!-- span class="pull-right">
								<span class="options" >
								</span>
							</span -->
						</div>
						<ul class="nav nav-tabs">
							<li class="active"><a href="#tab1" data-toggle="tab"><i class="icon-globe"></i>Indexing</a></li>
							<li class=""><a href="#tab2" data-toggle="tab"><i class="icon-search"></i>Searching</a></li>
						</ul>
						<div class="tab-content" ng-controller="ServersController">
							<!-- Start: Indexing tab and graph -->
							<div class="tab-pane active" id="tab1">
								<div class="separator">
									<!-- Threads -->
									<div class="inner-well clearfix">
										<div class="pull-left"><b>Jobs running : </b>{{servers[0].threadsRunning}}</div>
										<div class="pull-right">
											<a class="button mini rounded inset light-gray" ng-click="terminateThreads = !terminateThreads" ng-show="server.threadsRunning">Stop</a>
											<a class="button mini rounded inset light-gray" ng-click="terminateThreads = !terminateThreads" ng-show="!server.threadsRunning">Start</a>
										</div>
									</div>
									<div class="inner-well clearfix" ng-show="terminateThreads">
										<div ng-show="true">
											<div class="inner-well clearfix">
												<b>Are you sure?</b>
												<div class="pull-right">
													<input id="gtnDu" class="checky" type="checkbox" />
													<label for="gtnDu" class="checky green" ng-click="terminateThreadsConfirmed = !terminateThreadsConfirmed"><span></span></label>
												</div>
											</div>
											<div class="clearfix vpadded" ng-show="terminateThreadsConfirmed">
												<div class="pull-left">
													<label 
														for="gtnDu" 
														href="#"
														class="button red" 
														ng-click="
														    terminateThreads = !terminateThreads;
														    terminateThreadsConfirmed = !terminateThreadsConfirmed;
														    toggleThreadsRunning();"
                                                        ng-show="server.threadsRunning">Terminate jobs</label>
													<label
														for="gtnDu"
														href="#"
														class="button red"
														ng-click="
														    terminateThreads = !terminateThreads;
														    terminateThreadsConfirmed = !terminateThreadsConfirmed;
														    toggleThreadsRunning();"
                                                        ng-show="!server.threadsRunning">Start jobs</label>
												</div>
												<div class="pull-right">
													<label for="gtnDu" href="#" class="button gray" ng-click="terminateThreads = false;terminateThreadsConfirmed = false;">Cancel</label>
												</div>
											</div>
										</div>
									</div>
									<!-- Cpu -->
									<div class="inner-well clearfix">
										<div class="pull-left"><b>Cpu throttling : </b>{{servers[0].cpuThrottling}}</div>
										<div class="pull-right">
											<a class="button mini rounded inset light-gray" ng-click="terminateThrottling = !terminateThrottling" ng-show="server.cpuThrottling">Stop</a>
											<a class="button mini rounded inset light-gray" ng-click="terminateThrottling = !terminateThrottling" ng-show="!server.cpuThrottling">Start</a>
										</div>
									</div>
									<div class="inner-well clearfix" ng-show="terminateThrottling">
										<div ng-show="true">
											<div class="inner-well clearfix">
												<b>Are you sure?</b>
												<div class="pull-right">
													<input id="gtnCpu" class="checky" type="checkbox" />
													<label for="gtnCpu" class="checky green" ng-click="terminateThrottlingConfirmed = !terminateThrottlingConfirmed"><span></span></label>
												</div>
											</div>
											<div class="clearfix vpadded" ng-show="terminateThrottlingConfirmed">
												<div class="pull-left">
													<label 
														for="gtnCpu" 
														href="#" 
														class="button red"
                                                        ng-show="server.cpuThrottling"
														ng-click="
															terminateThrottling = false;
															terminateThrottlingConfirmed = false;
															toggleCpuThrottling();">Stop throttling</label>
													<label
														for="gtnCpu"
														href="#"
														class="button red"
                                                        ng-show="!server.cpuThrottling"
														ng-click="
															terminateThrottling = false;
															terminateThrottlingConfirmed = false;
															toggleCpuThrottling();">Start throttling</label>
												</div>
												<div class="pull-right">
													<label for="gtnCpu" href="#" class="button gray" ng-click="terminateThrottling = false;terminateThrottlingConfirmed = false;">Cancel</label>
												</div>
											</div>
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
		</div>
	</div>
</div>