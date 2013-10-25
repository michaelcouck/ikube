<%@ page isErrorPage = "true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table>
	<tr>
		<td>
			Request that failed: ${pageContext.errorData.requestURI}
		</td>
	</tr>
	<tr>
		<td>
			Status code: ${pageContext.errorData.statusCode}
		</td>
	</tr>
	<tr>
		<td>
			Exception: ${pageContext.errorData.throwable}<br>
			Error data: ${pageContext.errorData}
		</td>
	</tr>
	<tr>
		<td>
			Servlet name: ${pageContext.errorData.servletName}
		</td>
	</tr>
	<% 
		if (exception != null) {
			exception.printStackTrace();
		} 
	%>
</table>