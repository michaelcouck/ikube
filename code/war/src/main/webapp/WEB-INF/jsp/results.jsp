<%-- <%@page errorPage="/WEB-INF/jsp/error.jsp" contentType="text/html" %> --%>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">Results</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	
	<jsp:include page="include.jsp" flush="true" />
	
	<tr>
		<td class="bottom-content">Results brought to you by Ikube</td>
	</tr>
</table>