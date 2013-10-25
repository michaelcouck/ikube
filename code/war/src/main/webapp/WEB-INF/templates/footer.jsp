<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="ikube" uri="http://ikube"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="row-fluid">
	<div class="span12">
		<div class="footer">
			<div>Powered by: <a href="http://www.ikube.be/site" alt="Ikube Data Processing Platform">Ikube</a></div>
			<div>&copy; 2013 - Version - ${ikube:version()} - ${ikube:timestamp()}</div>
		</div>
	</div>
</div>