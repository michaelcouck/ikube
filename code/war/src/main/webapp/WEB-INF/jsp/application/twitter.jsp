<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<form>
<div class="container-fluid" ng-controller="TwitterController" ng-init="doConfig('searchTwitterFormConfig');">
	<div class="row-fluid">
		<div class="span4">

            <div class="row-fluid">
                <div style="margin-bottom: 20px;">
                    <div class="big-button-bar">
                        <a class="button large" href="#" ng-click="doTwitterSearch();"><i class="icon-search"></i><span>Analysis</span></a>
                        <a class="button large" href="#" onClick="modal('#results-modal');"><i class="icon-zoom-in"></i><span>Results</span></a>
                        <%--<a class="button large" href="#" onClick="modal('#results-modal');"><i class="icon-zoom-in"></i>Results</a>--%>
                    </div>
                </div>
            </div>
		</div>

        <div class="span8">
            <div class="row-fluid">
                <div class="tabbable black-box" style="margin-bottom: 18px;">
                    <div class="tab-header">
                        Analysis statistics
                        <img
                            ng-show="!status"
                            alt="Loading spinner"
                            src="<c:url value="/assets/images/loading.gif" />"
                            height="16px"
                            width="16px" >
						<span class="pull-right">
							<span class="options" ng-controller="ServersController">
								<div class="btn-group">
                                       <a class="dropdown-toggle" data-toggle="dropdown">
                                           <i class="icon-cog"></i>
                                       </a>
                                       <ul class="dropdown dropdown-menu black-box-dropdown dropdown-left" style="font-size: small;">
                                           <li><a href="#" ng-click="refreshServers();"><i class="icon-refresh">&nbsp;</i>Refresh</a></li>
                                       </ul>
                                   </div>
							</span>
						</span>
                    </div>

                    <div>
                        <div>
                            <div class="padded" ng-hide="!!statistics && !!statistics.positive && !!statistics.negative">
                                <i class="icon-arrow-left">&nbsp;</i> <b>Click the button on the far left... :)</b>
                            </div>
                            <div class="padded" ng-show="!!statistics && !!statistics.positive && !!statistics.negative">
                                <div class="user-content">
                                    <b>Duration : </b>'{{statistics.duration}}' milliseconds
                                    <b>Results :</b> '{{statistics.positive}}' positive and '{{statistics.negative}}' negative
                                </div>
                                <div class="user-content">
                                    <span ng-repeat="searchString in search.searchStrings track by $index">
										<span ng-show="!!searchString">
                                            <b>Term : </b>'{{searchString}}',
                                            <b>field : </b>{{search.searchFields[$index]}},
                                            <b>type : </b>{{search.typeFields[$index]}},
                                            <b>occurrence : </b>must
                                        </span>
									</span>
                                </div>
                                <div class="user-content"
                                     ng-show="!!search.coordinate && search.coordinate.latitude != coordinate.latitude">
                                    <b>Coordinate : </b> [{{search.coordinate.latitude}}, {{search.coordinate.longitude}}]
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
	</div>

	<div class="row-fluid">
		<div class="span4">
			<div class="box">
				<div class="tab-header">Filters</div>
				<div class="row-fluid">
					<div class="padded">
                        <span class="note pull-right"><b>Filter sentiment results by this term(s)</b></span>
                        <div
                            ng-controller="TypeaheadController"
                            ng-init="doConfig('searchTwitterFormConfig');">
                            <div class="append-transparent fill-up">
                                <input
                                    type="text"
                                    class="input-transparent"
                                    focus-me="true"
                                    ng-model="searchString"
                                    placeholder="Find this..."
                                    typeahead="result for result in doSearch()"
                                    typeahead-min-length="0"
                                    typeahead-wait-ms="500"
                                    typeahead-on-select="setSearchStrings([stripTags(searchString)]);"
                                    ng-change="setSearchStrings([stripTags(searchString)]);">
                            </div>
                        </div>
						<span class="note pull-right"><b>Hours of twitter history ({{search.startHour}})</b></span>
						<div ui-slider min="-168" max="0" ng-model="search.startHour" style="margin-top: 22px;"></div>

						<span class="note pull-right" style="margin-top: 22px;">
							<b>Around the point...(optional)</b>
						</span><br>
						<div class="input" style="margin-top: 30px;">
							<a class="button blue mini" ng-click="doShowMap(!showMap);"><i class="icon-globe"></i>&nbsp;Map</a>
							<a class="button green mini" ng-click="doClearCoordinate();"><i class="icon-remove-circle"></i>&nbsp;Clear</a>
						</div>

						<div class="input" style="margin-top: 10px; margin-bottom: 10px;" ng-show="showMap">
							<input type="text" ng-model="search.coordinate.latitude" value="{{search.coordinate.latitude}}" style="width: 100%;">
							<input type="text" ng-model="search.coordinate.longitude" value="{{search.coordinate.longitude}}" style="width: 100%;">
							<div id="map_canvas" google-map style="margin-top: 10px; margin-bottom: 10px;"></div>
						</div>

						<span class="note pull-right" style="margin-top: 22px;">
							<b>By language...(optional)</b>
						</span><br>
						<div class="input" style="margin-top: 30px;">
							<a class="button blue mini" ng-click="showLanguages = !showLanguages"><i class="icon-globe"></i>&nbsp;Languages</a>
						</div>

						<div ng-show="showLanguages">
							<div style="margin-top: 10px;">
								<a href="#" ng-click="search.searchStrings[languageIndex] = ''">All</a>
							</div>
							<div style="margin-top: 3px;" ng-repeat="language in languages">
								<a href="#" ng-click="search.searchStrings[languageIndex] = language">{{language}}</a>
							</div>
						</div>

						<div style="margin-bottom: 45px;"></div>

					</div>
				</div>
			</div>
		</div>

		<div class="span8">
			<div class="box tex">
				<div class="tab-header">Sentiment timeline</div>
				<div id="chart_div" style="width: 100%; height: 250px;"></div>
			</div>
		</div>
	</div>

	<jsp:include page="/WEB-INF/jsp/modal/results.jsp" />

</div>
</form>