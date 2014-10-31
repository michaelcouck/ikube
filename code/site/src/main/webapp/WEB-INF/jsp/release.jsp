<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url var="downloads" value="/downloads.html" />

<div id="maincontent">

	<h2>Releases</h2>
	
	<table>
		<tr>
			<th>Date</th>
			<th>Release</th>
			<th>Changes</th>
		</tr>
		<tr>
			<td>20.08.13</td>
			<td><a href="<c:out value="${downloads}" />">4.3.0</a></td>
			<td>
				* Introduced analytics and machine learning
				* Added the Twitter handler
				* Included a Twitter sentiment analysis strategy
				* Migrated to the fork/join for threading
				* Re-implemented the query builder for the database handler
			</td>
		</tr>
		<tr>
			<td>18.16.13</td>
			<td><a href="<c:out value="${downloads}" />">4.2.1</a></td>
			<td>
				* Fixed the Lucene null pointer in the range query comparator<br>
			</td>
		</tr>
		<tr>
			<td>15.03.13</td>
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