<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<security:authorize access="isAuthenticated()">
	<div class="well" style="width: 200px; padding: 8px;">
		<ul class="nav nav-tabs nav-stacked">
			<li class="nav-header">Functions</li>

			<li class="dropdown">
				<a class="dropdown-toggle" data-toggle="dropdown" href="#">Dash<b class="caret"></b></a>
				<ul class="dropdown-menu">
					<li><a href="<c:url value="/dash.html" />">Servers and actions</a></li>
					<li><a href="#" ng-click="show = !show">Indexing and searching graphs</a></li>
				</ul>
			</li>

			<li><a href="<c:url value="/analytics.html" />">Analytics</a></li>
			<li><a href="<c:url value="/search.html" />">Search</a></li>

			<li class="nav-header">Administration</li>
			<li><a href="<c:url value="/indexes.html" />">Indexes</a></li>
			<li><a href="<c:url value="/properties.html" />">Properties</a></li>
			<li><a href="<c:url value="/admin.html" />">Admin</a></li>

			<li class="nav-header">Default apps</li>
			<li><a href="<c:url value="/twitter.html" />">Twitter</a></li>
			<li><a href="<c:url value="/happy.html" />">Happy planet</a></li>
		</ul>
	</div>
</security:authorize>
