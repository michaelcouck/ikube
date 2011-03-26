<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
	<head>
		<title>Ikokoon</title>
		<link rel="shortcut icon" href="/images/icon.ico">
		<script src="http://www.google-analytics.com/ga.js" type="text/javascript"></script>
	</head>
	<script type="text/javascript">
		try {
			var pageTracker = _gat._getTracker("UA-13044914-4");
			pageTracker._trackPageview();
		} catch(err) {}
	</script>
	<body>
		<%-- Redirected because we can't set the welcome page to a virtual URL. --%>
		<c:redirect url="/index.html"/>
	</body>
</html>