<%--suppress ALL --%>
<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span6">
            <%-- ng-controller="ServersController" --%>
            <div class="nav-menu box">
                <ul class="nav nav-list">
                    <li class="active">
                        <a href="#"><i class="icon-cloud"></i>Servers</a>
                    </li>
                    <%-- ng-repeat="server in servers" --%>
                    <li>
                        <%-- ng-click="server.show = !server.show" --%>
                        <a href="#">
                            <i class="icon-globe"></i>{{server.address}}
                            <span class="pull-right badge" title="Cpu load">{{server.averageCpuLoad}}</span>
                        </a>
                        <%-- ng-show="server.show" --%>
                        <ul class="nav nav-list" style="margin-left: 10px; padding: 5px;">
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
        </div>

        <div class="span6" ng-show="editing();">
            <div class="black-box tex">
                <div class="tab-header">Index : {{indexContext.name}}</div>
                <ul class="recent-comments">
                    <li class="separator">
                        <div class="article-post">
                            <jsp:include page="../system/index.jsp"/>
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
                            <jsp:include page="../system/index.jsp"/>
                        </div>
                    </li>
                </ul>
            </div>
        </div>

    </div>
</div>