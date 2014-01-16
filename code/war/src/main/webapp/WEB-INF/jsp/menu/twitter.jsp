<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<ul class="secondary-nav-menu">

    <li href="happy" active-link="active">
        <a href="<c:url value="/application/happy.html" />">
            <i class="icon-globe"></i>Geo Tweets
        </a>
    </li>
	<li href="twitter" active-link="active">
		<a href="<c:url value="/application/twitter.html" />">
			<i class="icon-random"></i>Search & Analyze
		</a>
	</li>
	<%--<li href="tweets">
		<a href="#">
			Running tweets here...
		</a>
	</li>--%>
	
</ul>