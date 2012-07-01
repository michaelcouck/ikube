<%@ page isErrorPage="true" contentType="text/html" %>
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
			Exception: ${pageContext.errorData.throwable}
		</td>
	</tr>
	<tr>
		<td class="td-content">
			${pageContext.errorData.servletName}
		</td>
	</tr>
</table>