<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<html>
	<head>
		<meta http-equiv="Expires" content="-1">
		<meta http-equiv="Pragma" content="no-cache">
		<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
		
		<title><tiles:insertAttribute name="title" /></title>

		<%-- <link rel="shortcut icon" href="<c:url value="/images/icon.ico"/>" /> --%>
		
		<meta name="Keywords" content="Ikube, Enterprise Search, Web Site Search, Database Search, High Volume" />
		<meta name="Description" content="Ikube Enterprise Search." />
		
		<script src="http://www.google-analytics.com/ga.js" type="text/javascript"></script>
		<script src="http://maps.google.com/maps/api/js?sensor=false" type="text/javascript"></script>
	</head>

	<body onload="JavaScript:timedRefresh(10000);">
		<script type="text/javascript">
			try {
				var pageTracker = _gat._getTracker("UA-13044914-4");
				pageTracker._trackPageview();
			} catch(err) {
				document.write('<!-- ' + err + ' -->');
			}
		</script>
		<table>
			<tr>
				<td width="100%">
					<tiles:insertAttribute name="header" />
				</td>
			</tr>
			<tr>
				<td width="100%">
					<tiles:insertAttribute name="content" />
				</td>
			</tr>
			<tr>
				<td width="100%">
					<tiles:insertAttribute name="footer" />
				</td>
			</tr>
		</table>
	</body>
</html>