<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ikube" uri="http://ikube" %>

<!-- Set the refresh interval from the parameters -->
<c:if test="${not empty param.refreshinterval}">
	<c:set var="refreshinterval" scope="session" value="${param.refreshinterval}" />
</c:if>
<c:if test="${empty sessionScope.refreshinterval}">
	<c:set var="refreshinterval" scope="session" value="3" />
</c:if>
<c:if test="${sessionScope.refreshinterval <= 1 }">
	<c:set var="refreshinterval" scope="session" value="1" />
</c:if>

<script language="JavaScript" type="text/javascript">
// CREDITS:
// Automatic Page Refresher by Peter Gehrig and Urs Dudli
// Permission given to use the script provided that this notice remains as is.
// Additional scripts can be found at http://www.fabulant.com & http://www.designerwiz.com
// fabulant01@hotmail.com
// 8/30/2001

// IMPORTANT: 
// If you add this script to a script-library or script-archive 
// you have to add a link to http://www.fabulant.com on the webpage 
// where this script will be running.

////////////////////////////////////////////////////////////////////////////
// CONFIGURATION STARTS HERE

// Configure refresh interval (in seconds)
var refreshinterval=<c:out value="${sessionScope.refreshinterval}" />

// Shall the coundown be displayed inside your status bar? Say "yes" or "no" below:
var displaycountdown="yes"

// CONFIGURATION ENDS HERE
////////////////////////////////////////////////////////////////////////////

// Do not edit the code below
var starttime
var nowtime
var reloadseconds=0
var secondssinceloaded=0

function starttime() {
	starttime=new Date()
	starttime=starttime.getTime()
	countdown()
}
function countdown() {
	nowtime= new Date()
	nowtime=nowtime.getTime()
	secondssinceloaded=(nowtime-starttime)/1000
	reloadseconds=Math.round(refreshinterval-secondssinceloaded)
	if (refreshinterval>=secondssinceloaded) {
		var timer=setTimeout("countdown()",1000)
		if (displaycountdown=="yes") {
			window.status="Page refreshing in "+reloadseconds+ " seconds"
		}
	} else {
		clearTimeout(timer)
		window.location.reload(true)
	} 
}
window.onload=starttime
</script>

<spring:message code="refresh.rate" />&nbsp;${sessionScope.refreshinterval}&nbsp;
<c:choose>
	<c:when test="${empty pageContext.request.queryString}">
		<c:set var="uri" value="${pageContext.request.pathInfo}?" />
	</c:when>
	<c:otherwise>
		<c:set var="uri" value="${pageContext.request.pathInfo}?" />
		<c:forEach var="entry" items="${pageContext.request.parameterMap}">
			<c:if test="${entry.key ne 'refreshinterval'}">
				<c:set var="uri" value="${ikube:concat(uri, entry.key)}" />
				<c:set var="uri" value="${ikube:concat(uri, '=')}" />
				<c:set var="uri" value="${ikube:concat(uri, entry.value[0])}" />
				<c:set var="uri" value="${ikube:concat(uri, '&')}" />
			</c:if>
		</c:forEach>
	</c:otherwise>
</c:choose>
<a href="<c:url value="${uri}refreshinterval=${sessionScope.refreshinterval - 1}" />">-</a>
<a href="<c:url value="${uri}refreshinterval=${sessionScope.refreshinterval - 5}" />">--</a>
<a href="<c:url value="${uri}refreshinterval=${sessionScope.refreshinterval + 1}" />">+</a>
<a href="<c:url value="${uri}refreshinterval=${sessionScope.refreshinterval + 5}" />">++</a>