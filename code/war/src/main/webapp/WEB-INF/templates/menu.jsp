<%@ taglib prefix="search" uri="http://ikube/search" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript">
	window.onload = function() {
		document.searchForm.searchStrings.focus();
	}
</script>

<div id="sidebar">
	<ul>
		<li id="search">
			<form name="searchForm" id="searchForm" action="<c:url value="/results.html"/>">
				<fieldset>
					<input type="hidden" name="indexName" value="ikube">
					<input type="hidden" name="fragment" value="true">
					<input type="hidden" name="searchFields" value="content">
					<input type="hidden" name="searchUrl" value="http://localhost:9000/ikube/SearchServlet">
					<input type="text" name="searchStrings" id="search-text"
						value="<c:out value='${param.searchStrings}' />" />
					<input type="submit" id="search-submit" value="Go" />
				</fieldset>
			</form>
			<search:spellingTag>
				<script type="text/javascript">
					function submitSearchForm(searchStrings) {
						document.searchForm.searchStrings.value = searchStrings;
						document.searchForm.submit();
					}
				</script>
				Did you mean : <a href="JavaScript:submitSearchForm('<search:spellingWriterTag />')"><search:spellingWriterTag /></a>
			</search:spellingTag>
		</li>
		<li>
			<h2>Navigation</h2>
			<ul>
				<li><a href="<c:url value="/index.html"/>">Home</a></li>
				<li><a href="<c:url value="/search.html"/>">Search</a></li>
				<li><a href="<c:url value="/monitoring.html"/>">Monitoring</a></li>
				<li><a href="<c:url value="/administration.html"/>">Administration</a></li>
				<li><a href="<c:url value="/documentation.html"/>">Documentation</a></li>
			</ul>
		</li>
		<li>
			<h2>Interesting Java Links</h2>
			<ul>
				<li><a href="http://jenkins-ci.org/">Jenkins CI</a></li>
				<li><a href="http://www.springbyexample.org">Spring by Example</a></li>
				<li><a href="http://en.wikipedia.org/wiki/Software_package_metrics">Code Metrics</a></li>
				<li><a href="http://en.wikipedia.org/wiki/PageRank">Page Rank</a></li>
			</ul>
		</li>
	</ul>
</div>