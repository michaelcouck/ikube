<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<ul class="secondary-nav-menu">

	<li href="analyze" active-link="active">
		<a href="<c:url value="/analytics/analyze.html" />">
			<i class="icon-random"></i>Analyze
		</a>
	</li>
	
	<li href="create" active-link="active">
		<a href="<c:url value="/analytics/create.html" />">
			<i class="icon-upload-alt"></i>Create analyzer
		</a>
	</li>

	<li href="train" active-link="active">
		<a href="<c:url value="/analytics/train.html" />">
			<i class="icon-cogs"></i>Train analyzer
		</a>
	</li>

    <li href="configure" active-link="active">
        <a href="<c:url value="/analytics/configure.html" />">
            <i class="icon-forward"></i>Configure analyzer
        </a>
    </li>

</ul>