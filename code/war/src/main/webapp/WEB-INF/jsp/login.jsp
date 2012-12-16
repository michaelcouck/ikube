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

<ul class="tabs">
	<li class="active" rel="tab1">Login</li>
</ul>

<div class="tab_container">
	<div id="tab1" class="tab_content">
		<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		<form id="form" name="form" action="<spring:url value="/login" htmlEscape="true" />" method="POST">
		<c:if test="${not empty SPRING_SECURITY_LAST_EXCEPTION.message}"><spring:message code="login.failed" /><br></c:if>
		The default userid and password is user/user, guest/guest or administrator/administrator.<br><br>
		
		<spring:message code="login.user" /> : 
		<input id="j_username"  tabindex="1" type="text" name="j_username" value="">
		<spring:message code="login.password" /> : 
		<input id="j_password"  tabindex="2" type="password" name="j_password" /><br><br>
		<input 
			id="submit" name="submit" type="submit" 
			value="<spring:message code="login.loginbutton" />" 
			alt="<spring:message code="login.loginbutton" />" />
		</form>
	</div>
</div>

<script type="text/javascript">
	$(document).ready(function() {
		$(".tab_content").hide();
		$(".tab_content:first").show();
		$("ul.tabs li").click(function() {
			$("ul.tabs li").removeClass("active");
			$(this).addClass("active");
			$(".tab_content").hide();
			var activeTab = $(this).attr("rel");
			$("#" + activeTab).fadeIn();
		});
	});
</script>


