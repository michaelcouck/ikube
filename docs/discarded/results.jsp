<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
<script src="<c:url value="/js/jquery-1.4.4.min.js"/>"></script>
</head>


<c:set var="total" value="${param.total}" />
<c:set var="firstResult" value="${param.firstResult}" />
<c:set var="maxResults" value="${param.maxResults}" />

<c:set var="searchStrings" value="${param.searchStrings}" />
<c:set var="duration" value="${param.duration}" />

<c:set var="toResults" value="${total < firstResult + maxResults ? firstResult + (total % 10) : firstResult + maxResults}" />

<body>
	
	<c:if test="${!empty searchStrings && !empty total}">
				From : <c:out value='${firstResult + 1}' />,
				to : <c:out value='${toResults}' />,
				total : <c:out value='${total}' />,
				for '<c:out value='${searchStrings}' />',
				took <c:out value='${duration}' /> ms<br /><br>
			</c:if>
	
	
</body>

</html>
