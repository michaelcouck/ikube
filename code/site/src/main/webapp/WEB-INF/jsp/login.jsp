<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ page
	import="org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter"%>

<script type="text/javascript">
	// Focus on the first field in the form
	angular.element(document).ready(function() {
		doFocus('j_username');
	});
</script>

<div id="maincontent">
	<h2>Login</h2>

	<form id="form" name="form" action="<spring:url value="/login" htmlEscape="true" />" method="POST">
		<c:if test="${not empty SPRING_SECURITY_LAST_EXCEPTION.message}">
			<spring:message code="login.failed" /><br>
		</c:if>
		<table width="100%">
			<tr>
				<td width="100px"><spring:message code="login.user" />:</td>
				<td width="150px"><input size="150" id="j_username" tabindex="1" type="text" name="j_username" value=""></td>
			</tr>
			<tr>
				<td><spring:message code="login.password" />:</td>
				<td><input id="j_password" tabindex="2" type="password" name="j_password" /></td>
			</tr>
			<tr>
				<td>
					<input id="submit" name="submit" type="submit" value="<spring:message code="login.loginbutton" />" alt="<spring:message code="login.loginbutton" />" />
				</td>
			</tr>
		</table>
	</form>

</div>

