<%@page import="org.springframework.security.ui.savedrequest.SavedRequest"%>
<%@page import="org.springframework.security.ui.AbstractProcessingFilter"%>
<%
SavedRequest savedRequest = (SavedRequest) session.getAttribute(AbstractProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY);
if(savedRequest != null && savedRequest.getRequestURI().indexOf("/UIDL/") != -1) {
	response.setContentType("application/json; charset=UTF-8");
	//for(;;);[realjson]
	out.print("       {\"redirect\" : {\"url\" : \"" + request.getContextPath() + "/loginform.jsp" + "\"}} ");
} else {
	response.sendRedirect(request.getContextPath() + "/loginform.jsp");
}
%>