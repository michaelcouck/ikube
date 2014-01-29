<%--suppress ALL --%>
<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="container-fluid" ng-controller="AnalyticsController">
	<div class="row-fluid">
		<div class="span6">
			<div class="box">
				<div class="tab-header">
					Analytics 
					<img ng-show="!status" alt="Loading spinner" src="<c:url value="/assets/images/loading.gif" />" height="16px" width="16px">
					<span class="pull-right"><span class="options"><a href="#"><i class="icon-cog"></i></a></span></span>
				</div>

				<form class="fill-up">
					<div class="row-fluid">
						<div class="padded">
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
								analyzer. With clusterers there is no class attribute.
							</div>
							<div class="input">
								<textarea
									placeholder="Input data in Weka format...(essentially csv format)"
									rows="5" ng-model="analysis.input"
									title="datum,datum,datum..."></textarea>
							</div>
							<button type="submit" class="button blue" style="margin-top: 5px;" ng-click="doAnalysis()">&nbsp;Go</button>
						</div>
					</div>
				</form>
			</div>
		</div>

		<div class="span6">
			<div class="nav-menu box">
				<ul class="nav nav-list">
					<li class="active"><a href="#">
						<i class="icon-random"></i>
						Cluster/class/result <span class="pull-right badge blue" ng-bind-html-unsafe="analysis.clazz">{{analysis.clazz}}</span></a>
                    </li>
					<li>
						<a href="#" ng-click="showCorrelationCoefficients = !showCorrelationCoefficients">
							<i class="icon-book"></i>
							Correlation coefficients
							
							<ul class="nav nav-list" style="margin-left: 10px;" ng-show="showCorrelationCoefficients">
								<li ng-repeat="correlationCoefficient in analysis.correlationCoefficients">
									{{$index}} : 
									<div ng-repeat="coefficient in correlationCoefficient">
										&nbsp;&nbsp;{{coefficient}}
									</div>
								</li>
							</ul>
						</a>
					</li>
					<li>
						<a href="#" ng-click="showAlgorithmOutput = !showAlgorithmOutput">
							<i class="icon-folder-close"></i>
							Algorithm output 
							
							<ul class="nav nav-list" style="margin-left: 10px; padding: 5px;" ng-show="showAlgorithmOutput">
								<li>
									<i class="icon-th-list"></i>
									<span ng-bind-html-unsafe="analysis.algorithmOutput">{{analysis.algorithmOutput}}</span>
								</li>
							</ul>
						</a>
					</li>
					<li class="nav-header">Cluster statistics</li>
					<li><a href="#"><i class="icon-home"></i>...</a></li>
					<li><a href="#"><i class="icon-book"></i>...</a></li>
					<li><a href="#"><i class="icon-folder-close"></i>...</a></li>
					<li class="nav-header">Header...</li>
					<li><a href="#"><i class="icon-user"></i>...</a></li>
					<li><a href="#"><i class="icon-wrench"></i>...</a></li>
				</ul>
			</div>
		</div>
	</div>

	<div class="row-fluid">
		<div class="span12">
			<div class="box">
				<div class="tab-header">Cluster distribution</div>
				<div google-chart chart="chart" style="{{chart.cssStyle}}" />
			</div>
		</div>
	</div>

    <%--<div style="position: relative;">
        <a class="button blue icon-file" href="#"> Choose File... <input
                type="file"
                style="position: absolute; z-index: 2; top: 0; left: 0; opacity: 0; background-color: transparent; color: transparent;"
                file-upload multiple
                onchange="$('#upload-file-info').html($(this).val());" />
        </a> <span id="upload-file-info"></span>
    </div>--%>

</div>