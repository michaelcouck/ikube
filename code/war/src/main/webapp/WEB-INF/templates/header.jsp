					<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
					<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

					<table id="header" border="0">
						<tr>
							<td style="padding-left : 35px;" valign="bottom" width="50">
								<img alt="Ikube" src="<c:url value="/images/multi_ikube.jpg" />"  width="50" height="50">
							</td>
							<td valign="top" align="left">
								<a class="title" href="<c:url value="/index.html"/>">Ikube</a>
							</td>
							<td style="padding-bottom : 30px;" valign="top">
								<span style="font-size : 14px;">index the planet</span>
							</td>
							<td align="right" style="padding-bottom: 30px">
								<!--<a class="link" href="<c:url value="/search.html"/>">Search</a>
								<a class="link" href="<c:url value="/admin.html"/>">Administer</a>
								<a class="link" href="<c:url value="/admin.html"/>">Documentation</a>
								<a class="link" href="mailto:michaelcouck at hotmail dot com">Contact</a>-->
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
