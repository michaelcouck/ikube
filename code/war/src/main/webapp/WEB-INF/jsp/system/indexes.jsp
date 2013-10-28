<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="container-fluid" ng-controller="IndexContextsController">
	<div class="row-fluid">
		<div class="span12">
			<div class="box" style="position: relative;">
				<div class="tab-header">Indexes</div>
				<table id="data-table-json" class="table table-striped" style="cursor: pointer;"></table>
			</div>
		</div>
	</div>
</div>	

<jsp:include page="/WEB-INF/jsp/modal/crud.jsp" />