<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<nav id="primary" class="main-nav">
	<ul ng-controller="ActiveController">

        <li href="system" active-link="active"><a href="<c:url value="/system/dash.html" />"><i class="icon-cogs"></i>System</a></li>

        <li href="analytics" active-link="active"><a href="<c:url value="/analytics/analyze.html" />"><i class="icon-beaker"></i>Analytics</a></li>

        <li href="search" active-link="active"><a href="<c:url value="/search/search.html" />"><i class="icon-list-alt"></i>Search</a></li>

        <li href="apis" active-link="active"><a href="<c:url value="/documentation/apis.html" />"><i class="icon-cloud"></i>Apis</a></li>

        <li href="application" active-link="active"><a href="<c:url value="/application/happy.html" />"><i class="icon-twitter-sign"></i>Twitter</a></li>

        <li href="grid" active-link="active" ng-controller="NotificationController">
            <a href="#" ng-click="enterpriseNotification();"><i class="icon-th"></i>Grid</a>
        </li>

		<li class="dropdown">
			<a class="dropdown-toggle" data-toggle="dropdown">
				<i class="icon-share-alt"></i>More<span class="caret"></span>
			</a>
			<ul class="dropdown-menu">
				<li><a href="#" onClick="supportNotification();"><i class="icon-warning-sign"></i>Support</a></li>
				<li class="divider"></li>
				<li><a href="#"><i class="icon-file"></i>Documentation</a></li>
				<li><a href="#"><i class="icon-bolt"></i>Quick start</a></li>
				<li><a href="#"><i class="icon-bolt"></i>Creating indexes</a></li>
				<li class="divider"></li>
				<li>
					<a href="<spring:url value="/logout" htmlEscape="true" />" title="<spring:message code="security.logout" />">
						<i class="icon-off"></i><spring:message code="security.logout" />
					</a>
				</li>
			</ul>
		</li>
	</ul>
</nav>

<nav id="secondary" class="main-nav">
	<div class="profile-menu">
		<div class="pull-left">
			<div class="avatar">
				<img src="<c:url value="/assets/images/MYP_1376-small.jpg" />" />
			</div>
		</div>
		<div class="pull-left">
			<div class="title">Ikube</div>
			<div class="btn-group">
				<button class="button mini inset black">
					<i class="icon-search"></i>
				</button>
				<button class="button mini inset black">Projects</button>
				<button class="button mini inset black dropdown-toggle" data-toggle="dropdown">
					<i class="icon-cog"></i>
				</button>
					<ul class="dropdown-menu" style="font-size: small;">
						<li><a href="#" onClick="modal('#logs-modal');"><i class="icon-list-ol">&nbsp;</i>Server logs</a></li>
                        <li><a href="#" onClick="modal('#properties-modal');"><i class="icon-tasks">&nbsp;</i>System properties</a></li>
                        <%--<li class="divider"></li>--%>
                    </ul>
			</div>
		</div>
		<div class="pull-right profile-menu-nav-collapse">
			<button class="button black">
				<i class="icon-reorder"></i>
			</button>
		</div>
	</div>
	<tiles:insertAttribute name="sub-menu" />
</nav>

<!-- Include the modals for the input -->
<jsp:include page="/WEB-INF/jsp/modal/logs.jsp" />
<jsp:include page="/WEB-INF/jsp/modal/properties.jsp" />

<script type="text/html" id="template-notification">
<div class="notification animated fadeInLeftMiddle fast{{ item.itemClass }}">
	<div class="left">
		<div style="background-image: url({{ item.imagePath }})" class="{{ item.imageClass }}"></div>
	</div>
	<div class="right">
		<div class="inner">{{ item.text }}</div>
		<div class="time">{{ item.time }}</div>
	</div>
	<i class="icon-remove-sign hide"></i>
</div>
</script>

<script type="text/html" id="template-notifications">
<div class="container">
	<div class="row" id="notifications-wrapper">
		<div id="notifications"
			class="{{ bootstrapPositionClass }} notifications animated">
			<div id="dismiss-all" class="dismiss-all button blue">Dismiss all</div>
			<div id="content">
				<div id="notes"></div>
			</div>
		</div>
	</div>
</div>
</script>