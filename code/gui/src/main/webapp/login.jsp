<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.springframework.security.web.WebAttributes" %>
<%@ page import="org.springframework.security.web.savedrequest.SavedRequest" %>

<%-- <%
	SavedRequest savedRequest = (SavedRequest) session.getAttribute(WebAttributes.SAVED_REQUEST);
	if (savedRequest != null && savedRequest.getRedirectUrl().indexOf("/UIDL/") != -1) {
		System.out.println(savedRequest.getRedirectUrl());
		response.setContentType("application/json; charset=UTF-8");
		//for(;;);[realjson]
		out.print("       {\"redirect\" : {\"url\" : \"" + request.getContextPath() + "/loginform.jsp" + "\"}} ");
	} else {
		response.sendRedirect(request.getContextPath() + "/loginform.jsp");
	}
%> --%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Foo</title>
	<script language="JavaScript" type="text/javascript">
		window.onload=document.login-form.j_username.focus()
	</script>
</head>

<body>
<center>
	<form name="login-form" action="<c:url value='/j_spring_security_check'/>" method="POST">
		<table>
			<tr>
				<td colspan="2">
					You have tried to access a protected area of this application.
					By default you can also login as "user", with a password of "user".
				</td>
			</tr>
			
			<c:if test="${not empty param.login_error}">
			<tr>
				<td colspan="2">
					<font color="red">
						Your login attempt was not successful, try again.<br /> 
						Reason: <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}" />.
					</font>
				</td>
			</tr>
			</c:if>
						
			<tr>
				<td><label for="j_username">Name:</label></td>
				<td><input id="j_username" type='text' name='j_username' style="width: 150px" /></td>
			</tr>
			<tr>
				<td><label for="j_password">Password:</label></td>
				<td><input id="j_password" type='password' name='j_password' style="width: 150px" /></td>
			</tr>
			<tr>
				<td><input id="proceed" type="submit" value="Submit" /></td>
				<td><input id="reset"	type="reset" value="Reset" /></td>
			</tr>
		</table>
	</form>
</center>
</body>

</html>