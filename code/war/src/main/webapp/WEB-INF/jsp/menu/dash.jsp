<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<ul class="secondary-nav-menu">
	
	<li href="dash" active-link="active">
		<a href="<c:url value="/system/dash.html" />">
			<i class="icon-dashboard"></i>Dashboard
		</a>
	</li>
	
	<li href="indexes" active-link="active">
		<a href="<c:url value="/system/indexes.html" />">
			<i class="icon-list-ul"></i>Indexes
		</a>
	</li>
	
	<li href="crud" active-link="active">
		<a href="<c:url value="/system/crud.html" />">
			<i class="icon-th"></i>Database
		</a>
	</li>
	
	<!-- <li id="notification-full-image"><a href="#"><i class="icon-list-alt"></i>Notification #1</a></li>
	<li id="notification-small-image"><a href="#"><i class="icon-table"></i>Notification #2</a></li>
	<li><a id="notification-auto-dismiss" href="#"><i class="icon-share-alt"></i> With auto-dismiss</a></li> -->
</ul>