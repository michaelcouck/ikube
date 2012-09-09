<%@ page import="org.springframework.security.web.WebAttributes" %>
<%@ page import="org.springframework.security.web.savedrequest.SavedRequest" %>

<%
	SavedRequest savedRequest = (SavedRequest) session.getAttribute(WebAttributes.SAVED_REQUEST);
	if (savedRequest != null && savedRequest.getRedirectUrl().indexOf("/UIDL/") != -1) {
		System.out.println(savedRequest.getRedirectUrl());
		response.setContentType("application/json; charset=UTF-8");
		//for(;;);[realjson]
		out.print("       {\"redirect\" : {\"url\" : \"" + request.getContextPath() + "/loginform.jsp" + "\"}} ");
	} else {
		response.sendRedirect(request.getContextPath() + "/loginform.jsp");
	}
%>