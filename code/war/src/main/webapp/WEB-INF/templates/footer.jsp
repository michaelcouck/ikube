<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="ikube" uri="http://ikube"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="navbar navbar-fixed-bottom">
	<div class="navbar-inner">
		<div class="container">
			<table width="100%">
				<tr>
					<td align="center" nowrap="nowrap">
						<p>Made in Belgium</p>
					</td>
				</tr>
				<tr>
					<td align="center" nowrap="nowrap">
						<p>Version - ${ikube:version()} - ${ikube:timestamp()}</p>
					</td>
				</tr>
			</table>
		</div>
	</div>
</div>
