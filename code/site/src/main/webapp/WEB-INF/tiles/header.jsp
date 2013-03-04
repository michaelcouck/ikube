<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="header" class="header">
	<table width="100%" border="0">
		<tr>
			<td width="75%">
				<h1><a href="<c:url value="/index.html" />">Ikube</a></h1>
			</td>
			<td width="25%" style="float : right;">
					<form id="search-form" name="search-form" action="<c:url value="/results.html" />">
						<input id="searchString" name="searchString" value="${param.searchString}">
						<!-- <input type="submit" value="Go!"> -->
						<a onclick="JavaScript:document.getElementById('search-form').submit();">Go!</a>
					</form>
			</td>
		</tr>
	</table>
</div>