					<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
					<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

					<table id="header">
						<tr>
							<td style="padding-bottom : 10px; padding-left : 35px;">
								<a class="title" href="<c:url value="/index.html"/>">Ikube</a>
							</td>
							<td style="padding-bottom : 18px;">
								<span style="font-size : 14px;">
									imagine, <a href="<c:url value="/about.html"/>">About</a>
								</span>
							</td>
							<td align="right">
								<a class="link" href="<c:url value="/index.html"/>">Home</a>
								<a class="link" href="<c:url value="/about.html"/>">About</a>
								<a class="link" href="<c:url value="/search.html"/>">Search</a>
								<a class="link" href="<c:url value="/admin.html"/>">Administer</a>
								<a class="link" href="mailto:michaelcouck at hotmail dot com">Contact</a>
								&nbsp;&nbsp;

								<a href="<c:url value="/index.html"/>?language=english">
									<img alt="English" src="<c:url value="/images/flags/gb.gif"/>" title="English">
								</a>
								<a href="<c:url value="/index.html"/>?language=french" title="Français">
									<img alt="Français" src="<c:url value="/images/flags/fr.gif"/>">
								</a>
								<a href="<c:url value="/index.html"/>?language=german" title="Deutsch">
									<img alt="Deutsch" src="<c:url value="/images/flags/de.gif"/>">
								</a>
								<a href="<c:url value="/index.html"/>?language=dutch" title="Nederlands">
									<img alt="Nederlands" src="<c:url value="/images/flags/be.gif"/>">
								</a>
								<a href="<c:url value="/index.html"/>?language=russian" title="Русский">
									<img alt="Русский" src="<c:url value="/images/flags/ru.gif"/>">
								</a>

								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
							</td>
						</tr>
					</table>
