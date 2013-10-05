<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>

<%@ taglib prefix="ikube" uri="http://ikube" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!--
	This page is for the results. To enable this page please have a look at the SearchController to know
	what must be included in the model for the page. Typically it is things like the first and maximum results, 
	also the total results for the search and of course the results them selves which are a list of maps. 
 -->
 	<c:set var="icons" value="
		aac.png,avi.png,ai.png,aiff.png,bmp.png,c.png,cpp.png,css.png,dat.png,dotx.png,doc.png,dwg.png,
		dxf.png,eps.png,exe.png,flv.png,gif.png,h.png,hpp.png,html.png,ics.png,iso.png,java.png,jpg.png,
		key.png,mid.png,mp3.png,mp4.png,mpg.png,odf.png,ods.png,odt.png,otp.png,ots.png,ott.png,pdf.png,
		php.png,png.png,ppt.png,psd.png,py.png,qt.png,rar.png,rb.png,rtf.png,sql.png,tga.png,tgz.png,tiff.png,
		txt.png,wav.png,xls.png,xlsx.png,xml.png,yml.png,zip.png" />
	<c:set var="blank" value="blank.png" />
	<c:set var="toResults" value="${total < firstResult + maxResults ? firstResult + (total % 10) : firstResult + maxResults}" />
	<tr>
		<td>
			<c:if test="${!empty searchStrings && !empty total}">
				From : <c:out value='${firstResult + 1}' />,
				to : <c:out value='${toResults}' />,
				total : <c:out value='${total}' />,
				for '<c:out value='${searchStrings}' />',
				took <c:out value='${duration}' /> ms<br /><br>
			</c:if>
		</td>
	</tr>
	<tr>
		<td>
			<jsp:include page="/WEB-INF/jsp/pagination.jsp" flush="true" /> 
		</td>
	</tr>
	<tr>
		<td>
			<c:forEach var="result" items="${results}">
				<c:forEach var="entry" items="${result}">
					<c:out value="${entry.key}" /> : <c:out value="${entry.value}" escapeXml="false" />
					<c:if test="${entry.key == 'name' || entry.key == 'path' || entry.key == 'id'}">
						<img 
							alt="Document type" 
							src='<c:url value="/img/icons/${ikube:documentIcon(entry.value, icons, blank)}" />'
							height="15px"
							width="15px"
							align="bottom" />
					</c:if>
					<br>
				</c:forEach>
				<c:if test="${!empty indexName}">
					index : <c:out value="${indexName}" /><br>
				</c:if>
				<br>
			</c:forEach>
		</td>
	</tr>
	<jsp:include page="/WEB-INF/jsp/pagination.jsp" flush="true" />