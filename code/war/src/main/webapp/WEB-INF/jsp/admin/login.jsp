<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page errorPage="/WEB-INF/jsp/error.jsp" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter" %>

<script language="JavaScript" type="text/javascript">
	window.onload=document.form.j_username.focus()
</script>

<form name="form" action="<spring:url value="/login" htmlEscape="true" />" method="POST">
<table class="table-content" width="100%">
<tr>
	<td class="top-content" colspan="9" valign="middle">
		<span class="top-content-header">Login</span>
		<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
	</td>
</tr>
<tr>
	<td colspan="2">
		<c:if test="${not empty SPRING_SECURITY_LAST_EXCEPTION.message}">
			<spring:message code="login.failed" />
		</c:if>
	</td>
</tr>
<tr>
	<td colspan="2">
		The default userid and password is user/user, guest/guest or administrator/administrator.
	</td>
</tr>
<tr>
	<td colspan="2">
		&nbsp;
	</td>
</tr>
<tr>
	<td><spring:message code="login.user" /></td>
	<td><input id="search-text"  tabindex="1" type="text" name="j_username" value=""></td>
</tr>
<tr>
	<td><spring:message code="login.password" /></td>
	<td><input id="search-text"  tabindex="2" type="password" name="j_password" /></td>
</tr>
<tr>
	<td>
		<!-- id="login" -->
		<input id="search-submit-plain" name="submit" type="submit" 
			value="<spring:message code="login.loginbutton" />" 
			alt="<spring:message code="login.loginbutton" />" />
	</td>
</tr>
</table>
</form>