<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="targetSearchUrl" value="/results.html" />

<script type="text/javascript">
	window.onload = function() {
		document.ikubeSearchForm.searchStrings.focus();
	}
	
	$("#searchStrings").autocomplete({
	      source: function(request, response){
	           $.ajax({
	               type: "GET",
	               url: "<c:url value='/ikube/autocomplete'/>",
	               data: "{'Project_ID':'1'}",
	               contentType: "application/json; charset=utf-8",
	               dataType: "json",
	               success: function (msg) {
	                   response($.parseJSON(msg.d).Records);
	               },
	               error: function (msg) {
	                   alert(msg.status + ' ' + msg.statusText);
	               }
	           })
	       },
	       select: function (event, ui) {
	           $("#searchStrings").val(ui.item.Work_Item);
	           return false;
	       }
	}).data("autocomplete")._renderItem = function (ul, item) {
	    return $("<li></li>")
	    .data("item.autocomplete", item)
	    .append("<a>" + item.Work_Item + "</a>")
	    .appendTo(ul);
	};
	
</script>

<div id="sidebar" class="menu">
	<ul>
		<li id="search">
			<form name="ikubeSearchForm" id="ikubeSearchForm" action="<c:url value="${targetSearchUrl}"/>">
				<input name="targetSearchUrl" type="hidden" value="${targetSearchUrl}">
				<fieldset>
					<input type="text" name="searchStrings" id="search-text" value="<c:out value='${searchStrings}' />" />
					<input type="submit" id="search-submit" value="Go" />
				</fieldset>
			</form>
			<c:if test="${!empty corrections}">
				Did you mean : 
				<a href="<c:url value="${targetSearchUrl}" />?targetSearchUrl=${targetSearchUrl}&searchStrings=${corrections}">${corrections}</a><br>
			</c:if>
		</li>
		<li>
			<h2>Navigation</h2>
			<ul>
				<li><a href="<c:url value="/index.html"/>">Home</a></li>
				<li><a href="<c:url value="/admin/geosearch.html" />">Geo search</a></li>
				<li><a href="<c:url value="/admin/servers.html" />">Monitoring</a></li>
				<li><a href="<c:url value="/admin/actions.html?start=0&end=10" />">Actions</a></li>
				<li><a href="<c:url value="/documentation/index.html" />">Documentation</a></li>
			</ul>
		</li>
		<li>
			<h2>libraries used</h2>
			<ul>
				<li><a href="https://wiki.jenkins-ci.org/display/JENKINS/Serenity+Plugin">Serenity</a></li>
				<li><a href="http://lucene.apache.org/">Lucene</a></li>
				<li><a href="http://pdfbox.apache.org/">PdfBox</a></li>
				<li><a href="http://sourceforge.net/projects/c3p0/">C3p0</a></li>
				<li><a href="http://www.springsource.org">Spring</a></li>
				<li><a href="http://activemq.apache.org/">ActiveMQ</a></li>
				<li><a href="http://jenkins-ci.org/">Jenkins CI</a></li>
				<li><a href="http://www.h2database.com/html/main.html">H2 database</a></li>
				<li><a href="http://openjpa.apache.org/">OpenJpa</a></li>
				<li><a href="http://www.singularsys.com/jep/">Jep</a></li>
			</ul>
		</li>
		<li>
			<h2>similar products</h2>
			<ul>
				<li><a href="http://en.wikipedia.org/wiki/Apache_Solr">Solr</a></li>
				<li><a href="http://en.wikipedia.org/wiki/ISYS_Search_Software">Isys</a></li>
				<li><a href="http://en.wikipedia.org/wiki/Autonomy_Corporation">Autonomy</a></li>
				<li><a href="http://en.wikipedia.org/wiki/Google_Search_Appliance">Google Search Appliance</a></li>
				<li><a href="http://www.searchtools.com/info/database-search.html">Database search tools</a></li>
			</ul>
		</li>
		<li>
			<h2>interesting Java Links</h2>
			<ul>
				<li><a href="http://jenkins-ci.org/">Jenkins CI</a></li>
				<li><a href="http://www.springbyexample.org">Spring by Example</a></li>
				<li><a href="http://en.wikipedia.org/wiki/Software_package_metrics">Code Metrics</a></li>
				<li><a href="http://en.wikipedia.org/wiki/PageRank">Page Rank</a></li>
			</ul>
		</li>
		<security:authorize access="isAuthenticated()">
		<li>
			<h2><!-- Logout --></h2>
			<ul>
				<li>
					<spring:message code="security.logged.in.as" /><br>
					<security:authentication property="name" /><br>
					<%-- <security:authentication property="authorities" /><br> --%>
					<a href="<spring:url 
						value="/logout" htmlEscape="true" />" title="<spring:message 
						code="security.logout" />"><spring:message 
						code="security.logout" /></a>
				</li>
			</ul>
		</li>
		</security:authorize>
	</ul>
</div>