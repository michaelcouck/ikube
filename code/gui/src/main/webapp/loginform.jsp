<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Foo</title>
</head>
<body>
	<p>You have tried to access a protected area of this application.</p>
	<p>By default you can login as "admin", with a password of "admin".</p>
	<p>You can also login as "user", with a password of "user".</p>
	<%-- this form-login-page form is also used as the form-error-page to ask for a login again. --%> 
	<c:if test="${not empty param.login_error}">
		<font color="red">Your login attempt was not successful, tryagain.<br />
			Reason: <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}" />. 
		</font>
	</c:if>
	<form name="f" action="<c:url value='/j_spring_security_check'/>" method="POST">
		<label for="j_username">Name:</label> <input id="j_username" type='text' name='j_username' style="width: 150px" />
		<br />
		<label for="j_password">Password:</label> <input id="j_password" type='password' name='j_password' style="width: 150px" />
		<br />
		<input id="proceed" type="submit" value="Submit" /> <input id="reset" type="reset" value="Reset" />
	</form>
</body>
</html>
