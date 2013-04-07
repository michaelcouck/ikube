<%@ page isErrorPage = "true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content">
	<tr>
		<td class="td-content">
			Request that failed: ${pageContext.errorData.requestURI}
		</td>
	</tr>
	<tr>
		<td class="td-content">
			Status code: ${pageContext.errorData.statusCode}
		</td>
	</tr>
	<tr>
		<td class="td-content">
			Exception: ${pageContext.errorData.throwable}<br>
			Error data: ${pageContext.errorData}
		</td>
	</tr>
	<tr>
		<td class="td-content">
			Servlet name: ${pageContext.errorData.servletName}
		</td>
	</tr>
	<% 
		if (exception != null) {
			exception.printStackTrace();
		} 
	%>
</table>