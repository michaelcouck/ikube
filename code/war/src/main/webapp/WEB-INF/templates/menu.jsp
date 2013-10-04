<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<security:authorize access="isAuthenticated()">
	<div class="well" style="width: 200px; padding: 8px; margin-top: 50px;">
		<ul class="nav nav-tabs nav-stacked">

			<li class="nav-header">Functions</li>

			<li>
				<a href="<c:url value="/dash.html" />">Dash</a>
			</li>
			<li><a href="#" ng-click="show = !show">(graphs)</a></li>
			<li><a href="<c:url value="/analytics.html" />">Analytics</a></li>
			<li><a href="<c:url value="/search.html" />">Search</a></li>

			<li class="nav-header">Administration</li>

			<li><a href="<c:url value="/indexes.html" />">Indexes</a></li>
			<li><a href="<c:url value="/properties.html" />">Properties</a></li>

			<li class="nav-header">Default apps</li>
			
			<li><a href="<c:url value="/twitter.html" />">Twitter</a></li>
			<li><a href="<c:url value="/happy.html" />">Happy planet</a></li>
		</ul>
	</div>
</security:authorize>
