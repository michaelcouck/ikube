<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<div class="container-fluid" ng-controller="AnalyticsController">
	<div class="row-fluid">
		<div class="span6">
			<div class="box">
				<div class="tab-header">
					Analytics <img ng-show="!!status" alt="Loading spinner"
						src="<c:url value="/assets/images/loading.gif" />" height="16px"
						width="16px"> <span class="pull-right"> <span
						class="options"><a href="#"><i class="icon-cog"></i></span>
					</span>
				</div>

				<form class="fill-up">
					<div class="row-fluid">
						<div class="padded">
							<i class=" icon-random"></i> Analysis
							<div class="note pull-right">
								<b>Select an analyzer</b>
							</div>
							<div class="input search">
								<select ng-model="analysis.analyzer" ng-model="analyzers"
									ng-options="analyzer for analyzer in analyzers" class="fill-up">
									<option style="display: none" value="">Analyzer...</option>
								</select>
							</div>
							<div class="note large">
								<i class="icon-pencil"></i> Note: The first attribute is the
								class attribute, which you then omit in the case of a
								classifier. With clusterers there is no class attribute.
							</div>
							<div class="input">
								<textarea
									placeholder="Input data in Weka format...(essentially csv format)"
									rows="5" ng-model="analysis.input"
									title="datum,datum,datum... \n\rdatum,datum,datum..."></textarea>
							</div>
							<div style="position: relative;">
								<a class="button blue icon-file" href="#"> Choose File... <input
									type="file"
									style="position: absolute; z-index: 2; top: 0; left: 0; filter: alpha(opacity = 0); opacity: 0; background-color: transparent; color: transparent;"
									file-upload multiple
									onchange="$('#upload-file-info').html($(this).val());" />
								</a> <span id="upload-file-info"></span>
							</div>
							<button type="submit" class="button blue icon-random"
								style="margin-top: 5px;" ng-click="doAnalysis()">Go</button>
						</div>
					</div>
				</form>
			</div>
		</div>

		<div class="span6">
			<div class="box">
				<div class="tab-header">Analyzer output</div>
				<ul class="recent-comments">
					<li class="separator">
						<div class="article-post">
							<div class="user-content">
								<div google-chart chart="chart" style="" />
							</div>
						</div>
					</li>
				</ul>
				<ul class="recent-comments">
					<li class="separator">
						<div ng-show="!!analysis.clazz" style="font-weight: normal; width: 100%; height: 250px; overflow: auto;">
							<div class="note large">
								<i class="icon-pencil"></i> Note: This output is for all the instances/rows/data that were used in the input.
							</div>
							Class/cluster : <span style="color: blue; font-weight: bold;" ng-bind-html-unsafe="analysis.clazz"></span><br><br>
							<table class="table table-striped table-bordered table-condensed table-hover data-table">
								<tr ng-repeat="probability in analysis.output">
									<td>Cluster</td>
									<td>{{probability}}</td>
								</tr>
								<tr>
									<td>Algorithm output</td>
									<td><span ng-bind-html-unsafe="analysis.algorithmOutput"></td>
								</tr>
							</table>
							<!-- <div ng-repeat="probability in analysis.output">
							Cluster : {{$index}}, probability : {{probability}}
							<br><br> -->
							<!-- Algorithm output : <span ng-bind-html-unsafe="analysis.algorithmOutput"> -->
						</div>
					</li>
				</ul>
				<ul class="recent-comments">
					<li class="separator">
						<div class="article-post">
							<div class="user-content">
								<div class="btn-group">
									<button class="button mini" ng-repeat="page in pagination"
										ng-click="doPagedSearch(page.firstResult);">{{page.page}}</button>
								</div>
							</div>
						</div>
					</li>
				</ul>
			</div>
		</div>
	</div>
</div>