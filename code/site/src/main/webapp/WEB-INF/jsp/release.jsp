<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url var="downloads" value="/downloads.html" />

<div id="maincontent">

	<h2>Releases</h2>
	
	<table>
		<tr>
			<th>Release</th>
			<th>Changes</th>
		</tr>
		<tr>
			<td><a href="<c:out value="${downloads}" />">4.2.1</a></td>
			<td>
				* Fixed the Lucene null pointer in the range query comparator<br>
				* 
			</td>
		</tr>
		<tr>
			<td><a href="<c:out value="${downloads}" />">4.2.0</a></td>
			<td>
				* Fixed some fail over logic when the network file system fails<br>
				* Re-factored and cleaned the index manager logic<br>
				* Added a handler to index CSV files including geospatial data<br>
				* Added strategies for delta indexing and database/internet included data<br>
			</td>
		</tr>
	</table>
	
</div>