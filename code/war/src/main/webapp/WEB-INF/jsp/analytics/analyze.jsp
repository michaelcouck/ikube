<%--suppress ALL --%>
<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="container-fluid" ng-controller="AnalyticsController">
	<div class="row-fluid">
		<div class="span6">
			<div class="box">
				<div class="tab-header">
					Analyze
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
								<select
                                    ng-model="analysis.context"
                                    ng-model="contexts"
									ng-options="context for context in contexts"
                                    ng-change="doContext()"
                                    class="fill-up">
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
                                    rows="5"
									placeholder="Input data in Weka format...(essentially csv format)"
                                    ng-model="analysis.input"
									title="datum,datum,datum..."></textarea>
							</div>
							<button type="submit" class="button blue" style="margin-top: 5px; margin-bottom: 10px;" ng-click="doAnalysis()">&nbsp;Go</button>

                            <div class="note"><b>The context name for the analyzer in the system</b></div>
                            <div class="input">
                                <input
                                    class="input"
                                    type="text"
                                    focus-me="true"
                                    ng-model="context.name"
                                    placeholder="Identifier..."
                                    readonly>
                            </div>
                            <div class="note"><b>The analyzer wrapper class</b></div>
                            <div class="input">
                                <input
                                    class="input"
                                    type="text"
                                    ng-model="context.analyzer"
                                    placeholder="Analyzer identifier..."
                                    readonly>
                            </div>
                            <div class="note"><b>The filter class for the data</b></div>
                            <div class="input">
                                <input
                                    class="input"
                                    type="text"
                                    ng-model="context.filters"
                                    placeholder="Filter class..."
                                    readonly>
                            </div>
                            <div class="note"><b>The algorithm/logic for the analyzer</b></div>
                            <div class="input">
                                <input
                                    class="input"
                                    type="text"
                                    ng-model="context.algorithms"
                                    placeholder="Algorithm class..."
                                    readonly>
                            </div>
                            <div class="note"><b>The initialization options for the algorithm</b></div>
                            <div class="input">
                                <input
                                    class="input"
                                    type="text"
                                    ng-model="context.options"
                                    placeholder="The options, if any..."
                                    readonly>
                            </div>
                            <div class="note"><b>Whether this analyzer has been built or not</b></div>
                            <div class="input">
                                <input
                                    class="input"
                                    type="text"
                                    ng-model="context.built"
                                    placeholder="Is this analyzer's model built..."
                                    readonly>
                            </div>
						</div>
					</div>
				</form>
			</div>
		</div>

		<div class="span6">
            <div class="box">
                <div class="tab-header">
                    <b>Analysis output</b>
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
                            <div class="note">The result of the analysis</div>
                            <div class="input">
                                <input
                                    class="input"
                                    type="text"
                                    ng-model="analysis.clazz"
                                    placeholder="Analysis result..."
                                    readonly>
                            </div>
                            <div class="note">The output(if any) of the analyzer</div>
                            <div class="input">
                                <input
                                    class="input"
                                    type="text"
                                    ng-model="analysis.output"
                                    placeholder="Analysis output..."
                                    readonly>
                            </div>
                            <!-- div class="note">Classes or clusters</div>
                            <div class="input">
                                <input
                                    class="input"
                                    type="text"
                                    ng-model="analysis.classesOrClusters"
                                    placeholder="Classes or clusters..."
                                    readonly>
                            </div>
                            <div class="note">Sizes for classes or clusters</div>
                            <div class="input">
                                <input
                                    class="input"
                                    type="text"
                                    ng-model="analysis.sizesForClassesOrClusters"
                                    placeholder="Sizes for classes or clusters..."
                                    readonly>
                            </div -->
                            <div class="note">This area is the analyzer algorithm output for the analysis</div>
                            <div class="input">
                                <textarea
                                    placeholder="Analyzer algorithm output..."
                                    rows="15"
                                    ng-model="analysis.algorithmOutput"
                                    title="The output from the analysis algorithm"
                                    readonly></textarea>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
	</div>
</div>