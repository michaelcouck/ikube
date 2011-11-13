<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<html>
	<head>
		<title>Ikube</title>
		<!-- <meta http-equiv="refresh" content="5"> -->
		<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
		<meta name="Keywords" content="Michael Couck, Ikube, Enterprise Search, Web Site Search, Database Search, High Volume" />
		<meta name="Description" content="Ikube Enterprise Search." />
		<link rel="shortcut icon" href="<c:url value="/images/icon.ico"/>" />
		<link href="<c:url value="/style/style-white.css"/>" rel="stylesheet" type="text/css" media="screen" />
		<script type="text/javascript" src="<c:url value="/js/ikube.js" />"></script>
		<script src="http://www.google-analytics.com/ga.js" type="text/javascript"></script>
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
		<center>
			<table border="0" align="center" cellpadding="0" cellspacing="0">
				<tr>
					<td colspan="2">
						<tiles:insertAttribute name="header" />
					</td>
				</tr>
				<tr id="content">
					<td width="250">
						<tiles:insertAttribute name="menu" />
					</td>
					<td width="700">
						<tiles:insertAttribute name="content" />
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<tiles:insertAttribute name="footer" />
					</td>
				</tr>
			</table>
		</center>
	</body>
</html>