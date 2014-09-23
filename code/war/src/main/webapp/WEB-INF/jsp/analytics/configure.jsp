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
                        <b>Train analyzer</b>
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

                                <div class="input search">
                                    <select
                                        ng-model="analysis.context"
                                        ng-model="contexts"
                                        ng-options="context for context in contexts"
                                        ng-change="getContext();getData();"
                                        class="fill-up">
                                        <option style="display: none" value="">Analyzer...</option>
                                    </select>
                                </div>

                                <%--<div ng-repeat="data in someData">--%>
                                    <%--{{data}}--%>
                                    <%--<div ng-repeat="row in data">--%>
                                        <%--{{row}}--%>
                                    <%--</div>--%>
                                <%--</div>--%>

                                <div ng-repeat="matrix in matrices track by $index">
                                    <table ng-table>
                                        <tr>
                                            <th ng-repeat="column in matrix[$index] track by $index" nowrap>
                                                {{$index}} <input type="checkbox">
                                            </th>
                                        </tr>
                                        <tr ng-repeat="row in matrix track by $index">
                                            <td ng-repeat="column in row track by $index">{{column}}</td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <%--<div ng-repeat="matrix in matrices track by $index">--%>
        <%--<table ng-table>--%>
            <%--<tr>--%>
                <%--<th ng-repeat="column in matrix[$index] track by $index" nowrap>--%>
                    <%--{{$index}} <input type="checkbox">--%>
                <%--</th>--%>
            <%--</tr>--%>
            <%--<tr ng-repeat="row in matrix track by $index">--%>
                <%--<td ng-repeat="column in row track by $index">{{column}}</td>--%>
            <%--</tr>--%>
        <%--</table>--%>
    <%--</div>--%>
</div>