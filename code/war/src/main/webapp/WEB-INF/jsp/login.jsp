<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page import="org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter" %>

<script type="text/javascript">
// Focus on the first field in the form
angular.element(document).ready(function() {
	doFocus('j_username');
});
</script>

<span style="float: right;"><script type="text/javascript">writeDate();</script></span>

<form id="form" name="form" action="<spring:url value="/login" htmlEscape="true" />" method="POST" class="form-inline">
	<fieldset>
		<legend>Log in</legend>
		<c:if test="${not empty SPRING_SECURITY_LAST_EXCEPTION.message}"><spring:message code="login.failed" /><br>
			The default userid and password is user/user, guest/guest or administrator/administrator.<br><br>
		</c:if>
		<input id="j_username"  tabindex="1" type="text" name="j_username" class="input-small" placeholder="administrator">
		<input id="j_password"  tabindex="2" type="password" name="j_password" class="input-small" placeholder="administrator">
		<label class="checkbox"><input type="checkbox">Remember me</label>
		<button type="submit" class="btn" id="submit" name="submit"	alt="<spring:message code="login.loginbutton" />">Sign in</button>
	</fieldset>
</form>