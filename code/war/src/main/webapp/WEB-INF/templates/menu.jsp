<%@ taglib prefix="search" uri="http://ikube/search" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript">
	window.onload = function() {
		document.searchForm.searchString.focus();
	}
</script>

<div id="sidebar">
	<ul>
		<li id="search">
			<form name="searchForm" id="searchForm" action="<c:url value="/search/search.html"/>">
				<fieldset>
					<input type="hidden" name="name" value="Ikokoon">
					<input type="hidden" name="fragment" value="true">
					<input type="hidden" name="searchField" value="contents">
					<input type="text" id="search-text" name="searchString"
						value="<%=	request.getParameter("searchString") != null ? request.getParameter("searchString") : "" %>" />
					<input type="submit" id="search-submit" value="Go" />
				</fieldset>
			</form>
			<search:spellingTag>
				<script type="text/javascript">
					function submitSearchForm(searchString) {
						document.searchForm.searchString.value = searchString;
						document.searchForm.submit();
					}
				</script>
				Did you mean : <a href="JavaScript:submitSearchForm('<search:spellingWriterTag />')"><search:spellingWriterTag /></a>
			</search:spellingTag>
		</li>
		<li>
			<h2>Navigation</h2>
			<ul>
				<li><a href="<c:url value="/index.html"/>">Search</a></li>
				<li><a href="<c:url value="/index.html"/>">Administer</a></li>
				<li><a href="<c:url value="/index.html"/>">Servers</a></li>
				<li><a href="<c:url value="/index.html"/>">Monitoring</a></li>
				<li><a href="<c:url value="/index.html"/>">Documentation</a></li>
			</ul>
		</li>
		<li>
			<h2>Interesting Java Links</h2>
			<ul>
				<li><a href="http://hudson-ci.org/">Hudson CI</a></li>
				<li><a href="http://www.springbyexample.org">Spring by Example</a></li>
				<li><a href="http://en.wikipedia.org/wiki/Software_package_metrics">Code Metrics</a></li>
				<li><a href="http://en.wikipedia.org/wiki/PageRank">Page Rank</a></li>
			</ul>
		</li>
	</ul>
</div>