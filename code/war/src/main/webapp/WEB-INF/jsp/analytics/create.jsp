<%--suppress ALL --%>
<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div ng-controller="AnalyticsController">
	<div class="container-fluid">
		<div class="row-fluid">
			<div class="span12">
				<div class="box">
					<div class="tab-header">
						<b>Create analyzer</b>
						<img 
							ng-show="!status"
							alt="Loading spinner"
							src="<c:url value="/assets/images/loading.gif" />" 
							height="16px"
							width="16px">
							<span class="pull-right">
								<span class="options"><a href="#"><i class="icon-cog"></i></a>
							</span>
						</span>
					</div>
                    <%--target="/analytics/configure.html"--%>
                    <form class="fill-up" ng-controller="FileUploadController">
						<div class="row-fluid">
							<div class="padded">
                                <div class="note"><b>Specify the unique identifier for your analyzer...</b></div>
                                <div class="input">
                                    <input
                                        class="input"
                                        type="text"
                                        focus-me="true"
                                        ng-model="context.name"
                                        placeholder="Identifier...">
                                </div>
                                <div class="input">
                                    <select
                                        ng-model="context.analyzer"
                                        ng-model="analyzers"
                                        ng-options="analyzer for analyzer in analyzers"
                                        class="fill-up">
                                        <option style="display:none" value="">Analyzer type...</option>
                                    </select>
                                </div>
                                <div class="input">
                                    <select
                                        ng-model="context.algorithms"
                                        ng-model="algorithms"
                                        ng-options="algorithm for algorithm in algorithms"
                                        class="fill-up">
                                        <option style="display:none" value="">Algorithm type...</option>
                                    </select>
                                </div>
                                <div class="input">
                                    <select
                                        ng-model="context.filters"
                                        ng-model="filters"
                                        ng-options="filter for filter in filters"
                                        class="fill-up">
                                        <option style="display:none" value="">Filter type...</option>
                                    </select>
                                </div>

                                <div class="note">
                                    <b>Specify the options in string[], for algorithms please refer to
                                    the Neuroph and Weka documentation for options for the particular algorithm capabilities</b>
                                </div>
                                <div class="input">
                                    <input
                                        class="input"
                                        type="text"
                                        ng-model="context.options"
                                        placeholder="Options... (eg. '-N, 6', means six clusters for the clusterer">
                                </div>

                                <div class="note">
                                    <b>Select an input data file to train the model. Typically this is a csv file</b>
                                </div>
                                <input type="file" file-upload/>

                                <div class="note" style="margin-top: 15px;">
                                    <i class="icon-warning-sign"></i> Warning: You overwrite the original analyzer when submitted
                                </div>
                                <button type="submit" class="button blue" ng-click="doCreate();">Create</button>
							</div>
						</div>
					</form>
                </div>
			</div>
		</div>
	</div>
</div>