<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table ng-controller="ServersController" class="table table-condensed">
	<tr>
		<td>
			<div ng-show="server.cpuThrottling"><img src="<c:url value="/img/icons/open.gif" />">&nbsp;CPU throttling</div>
			<div ng-show="!server.cpuThrottling"><img src="<c:url value="/img/icons/red_square.gif" />">&nbsp;CPU throttling</div>
		</td>
		<td>
			<input 
					type="submit" 
					class="btn btn-small btn-warning" 
					value="Terminate"
					ng-click="toggleCpuThrottling();"
					title="Toggle the CPU throttling">
		</td>
	</tr>
	<tr>
		<td>
			<div ng-show="server.threadsRunning"><img src="<c:url value="/img/icons/open.gif" />">&nbsp;Threads running</div>
			<div ng-show="!server.threadsRunning"><img src="<c:url value="/img/icons/red_square.gif" />">&nbsp;Threads running</div>
		</td>
		<td>
			<input 
					type="submit" 
					class="btn btn-small btn-warning" 
					value="Terminate"
					ng-click="terminateAll();"
					title="Toggle the threads running">
		</td>
	</tr>
</table>

<table ng-controller="ServersController" class="table table-condensed">
	<tr>
		<th><img src="<c:url value="/img/icons/server.gif" />">&nbsp;Server log tails</th>
	</tr>
	<tr ng-repeat="server in servers">
		<td class="bordered" nowrap="nowrap" valign="bottom">
			<a ng-click="server.show=!server.show" href="#">
				<img src="<c:url value="/img/icons/web.gif" />">&nbsp;<b>Address</b> : {{server.address}} <br>
			</a>
			<div ng-show="!server.show">
				<textarea rows="10" cols="450" class="well" style="width: 750px;">{{server.logTail}}</textarea>
			</div>
		</td>
	</tr>
</table>