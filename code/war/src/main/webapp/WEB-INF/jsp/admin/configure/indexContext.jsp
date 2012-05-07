<%@ taglib prefix="ikube" uri="http://ikube" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">Index Context</span>
			&nbsp;<c:out value="${server.address}" />
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
</table>

<form:form action="/simple/indexContext" modelAttribute="indexContext">
<table width="100%">
	<tr>
		<td>Index name:</td>
		<td><form:input path="name" /></td>
	</tr>
	<tr>
		<td>Maximum age:</td>
		<td>${maxAge}</td>
	</tr>
	<tr>
		<td>Merge factor:</td>
		<td>${mergeFactor}</td>
	</tr>
	<tr>
		<td>Buffered documents:</td>
		<td>${bufferedDocs}</td>
	</tr>
	<tr>
		<td>Buffer size:</td>
		<td>${bufferSize}</td>
	</tr>
	<tr>
		<td>Max field length:</td>
		<td>${maxFieldLength}</td>
	</tr>
	<tr>
		<td>Compound file:</td>
		<td>${compoundFile}</td>
	</tr>
	<tr>
		<td>Batch size:</td>
		<td>${batchSize}</td>
	</tr>
	<tr>
		<td>Internet batch size:</td>
		<td>${internetBatchSize}</td>
	</tr>
	<tr>
		<td>Maximum read length:</td>
		<td>${maxReadLength}</td>
	</tr>
	<tr>
		<td>Index director path:</td>
		<td>${indexDirectoryPath}</td>
	</tr>
	<tr>
		<td>Index director path backup:</td>
		<td>${indexDirectoryPathBackup}</td>
	</tr>
	
	<tr>
		<td><input type="submit" id="submit" value="Go" /></td>
	</tr>
</table>
</form:form>

<table width="100%">
	<tr>
		<td class="td-content">
			<strong>index context</strong>&nbsp;
			Create an index context that will be the holder for indexables like web sites, databases etc. 
		</td>
	</tr>
</table>