<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript">
$(document).ready(function() {
	var forms = document.getElementsByTagName('form');
	setTimeout(function() {
		for ( var i = 0; i < forms.length; i++) {
			$('#' + forms[i].id).ajaxForm(function() {
				alert('Properties updated');
			});
		}
	}, 1000);
});
</script>

<div class="container-fluid">
	<div class="row-fluid">

		<div class="span6">
			<div class="box" style="position: relative;">
				<div class="tab-header">Indexes</div>
				<table class="table table-striped data-table" ng-controller="IndexContextsController">
					<thead>
						<tr>
							<th>Name</th>
							<th>Open</th>
							<th>Documents</th>
							<th>Size</th>
							<th>Timestamp</th>
						</tr>
					</thead>
					<tbody>
						<tr ng-repeat="indexContext in indexContexts">
							<td>{{indexContext.name}}</td>
							<td>{{indexContext.open}}</td>
							<td>{{indexContext.numDocsForSearchers / 1000000}}</td>
							<td>{{indexContext.snapshot.indexSize / 1000000}}</td>
							<td>{{indexContext.maxAge}}</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<div class="span6">
			<div class="row-fluid">
				<div class="span12">
					<div class="tabbable black-box" style="margin-bottom: 18px;">
						<div class="tab-header">
							Edit index 
							<span class="pull-right">
								<span class="options">
									<div class="btn-group">
										<a class="dropdown-toggle" data-toggle="dropdown">
											<i class="icon-cog"></i>
										</a>
										<ul class="dropdown-menu black-box-dropdown dropdown-left">
											<li><a href="#" ng-click="refreshServers();">Something here?</a></li>
											<li><a href="#">Another action</a></li>
											<li><a href="#">Something else here</a></li>
											<li class="divider"></li>
											<li><a href="#">Separated link</a></li>
										</ul>
									</div>
								</span>
							</span>
						</div>

						<ul class="nav nav-tabs">
							<li class="active"><a href="#tab1"><i class="icon-globe"></i>Indexing</a></li>
						</ul>
						<div class="tab-content" ng-controller="ServersController">
							<div class="tab-pane active" id="tab2">
								<div class="separator">
									<div class="inner-well">
										<textarea cols="450" rows="10" style="width: 95%;"></textarea>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
	
<%-- <form id="create" name="create" action="<c:url value="/service/database/entity/create" />" method="post">
<table ng-controller="CreateController" style="border : 1px solid #aaaaaa;" width="100%">
	<tr ng-model="entity" ng-repeat="(key, value) in entity" >
		<td>{{key}}</td>
		<td><input type="text" value="{{value}}"></td>
	</tr>
	<tr>
		<td>
			<input type="submit" ng-click="createEntity();" value="Create entity">
		</td>
	</tr>
</table>
</form> --%>