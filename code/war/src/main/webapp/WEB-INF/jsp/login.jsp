<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ page import="org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter"%>

<script type="text/javascript">
	// Focus on the first field in the form
	angular.element(document).ready(function() {
		doFocus('j_username');
	});
</script>

<body class="login">
<div class="container">
	<div class="login-wrapper" style="margin-top: 120px">
		<div id="login-manager">
			<div id="login" class="login-wrapper animated">
				<form id="form" name="form" action="<spring:url value="/login" htmlEscape="true" />" method="POST">
					<c:if test="${not empty SPRING_SECURITY_LAST_EXCEPTION.message}">
						<spring:message code="login.failed" /><br>
							The default userid and password is user/user, guest/guest or administrator/administrator.<br><br>
					</c:if>
					<div class="input-group">
						<input name="j_username" type="text" placeholder="administrator" class="input-transparent" />
						<input name="j_password" type="text" placeholder="administrator" class="input-transparent" />
					</div>
					<button id="login-submit" type="submit" class="login-button">Login</button>
				</form>
			</div>

			<div id="register" class="login-wrapper animated" style="display: none;">
				<form id="form" name="form" action="<spring:url value="/register" htmlEscape="true" />" method="POST">
					<div class="input-group">
						<input type="text" placeholder="email" class="input-transparent" />
						<input type="text" placeholder="first name" class="input-transparent" />
						<input type="text" placeholder="last name" class="input-transparent" />
						<input type="email" placeholder="confirm password" class="input-transparent" />
						<input type="password" placeholder="password" class="input-transparent" />
					</div>
					<button id="register-submit" type="submit" class="login-button">Register</button>
				</form>
			</div>

			<div id="forgot" class="login-wrapper animated" style="display: none;">
				<form id="form" name="form" action="<spring:url value="/recover" htmlEscape="true" />" method="POST">
					<div class="input-group">
						<input type="text" placeholder="email" class="input-transparent" />
					</div>
					<button id="forgot-submit" type="submit" class="login-button">Recover</button>
				</form>
			</div>

			<div class="inner-well" style="text-align: center; margin: 20px 0;">
				<a href="#" id="login-link" class="button mini rounded gray"><i class="icon-signin"></i>Login</a>
				<a href="#" id="register-link" class="button mini rounded gray"><i class="icon-plus"></i>Register</a>
				<a href="#" id="forgot-link" class="button mini rounded gray"><i class="icon-question-sign"></i>Forgot Password?</a>
			</div>
		</div>
	</div>
</div>

</body>
