<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter" %>

<body onload="document.form.j_username.focus();">
	<c:if test="${not empty SPRING_SECURITY_LAST_EXCEPTION.message}">
    	<div id="infomessage" class="error message" >
    		<ul>
    			<li>
    				<spring:message code="login.failed" />
    			</li>
    		</ul>
    	</div>
	</c:if>
	<form name="form" action="<spring:url value="/login" htmlEscape="true" />" method="POST">
	<table>
		<tr>
			<td><spring:message code="login.user" /></td>
			<td><input tabindex="1" type="text" name="j_username" value=""></td>
		</tr>
		<tr>
			<td><spring:message code="login.password" /></td>
			<td><input tabindex="2" type="password" name="j_password" /></td>
		</tr>
		<tr>
			<td>
				<input id="login" name="submit" type="submit" value="<spring:message code="login.loginbutton" />" alt="<spring:message code="login.loginbutton" />" />
			</td>
		</tr>
	</table>
	</form>
</body>
