<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<ul class="secondary-nav-menu">

	<li href="analyze" active-link="active">
		<a href="<c:url value="/analytics/analyze.html" />">
			<i class="icon-random"></i>Analyze
		</a>
	</li>
	
	<li href="admin" active-link="active">
		<a href="<c:url value="/analytics/admin.html" />">
			<i class="icon-upload-alt"></i>Analyzer admin
		</a>
	</li>
	
</ul>