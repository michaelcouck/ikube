<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div ng-controller="AnalyticsController">
	<div class="container-fluid">
        <div class="row-fluid">
            <div class="span12">
                <div class="box">
                    <div class="tab-header">
                        <b>Configure analyzer</b>
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
                    <form class="fill-up">
                        <div class="row-fluid">
                            <div class="padded">
                                <div class="note">
                                    <b>Select an analyzer to configure and re-build</b>
                                </div>

                                <div>
                                    <label>
                                        <select
                                            ng-model="analysis.context"
                                            ng-model="contexts"
                                            ng-options="context for context in contexts"
                                            ng-change="getContext();getData();"
                                            class="fill-up">
                                            <option style="display: none" value="">Analyzer...</option>
                                        </select>
                                    </label>
                                </div>

                                <div ng-show="!!matrices">
                                    <div class="note">
                                        <b>The table below contains some example of the data with the columns horizontal. Select the columns that
                                        you would like to ignore in the training set. Typically this would be columns that you think have no bearing
                                        on the other features, i.e. there is no correlation to the instances. For example if you are predicting the probability
                                        of rain, and in your data set is the colour of the weather woman's eyes, this probably does not have an influence on the
                                        weather, and is safe to ignore in the training. Note that this feature will also be ignored in the data to be analyzed.
                                        </b>
                                    </div>
                                    <div ng-repeat="matrix in matrices track by $index">
                                        <table ng-table width="100%" class="table table-striped table-condensed table-hover">
                                            <tr>
                                                <th width="100px">Exclude</th>
                                                <th colspan="200" class="id">Feature values</th>
                                            </tr>
                                            <tbody>
                                            <tr ng-repeat="row in matrix track by $index">
                                                <td><input type="checkbox" style="font-size: 20px;"></td>
                                                <td ng-repeat="column in row track by $index" nowrap>{{column}}</td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>