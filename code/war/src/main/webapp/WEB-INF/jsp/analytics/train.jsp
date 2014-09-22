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
                                    <b>Or select an analyzer to re-train</b>
                                </div>
                                <div class="input">
                                    <select
                                        ng-model="analysis.context"
                                        ng-model="contexts"
                                        ng-options="context for context in contexts" class="fill-up">
                                        <option style="display: none" value="">Analyzer...</option>
                                    </select>
                                </div>
                                <div class="note">
                                    Input the expected class(for classifiers and clusterers) or predicted outcome
                                </div>
                                <div class="input">
                                    <input
                                        type="text"
                                        ng-model="analysis.clazz"
                                        placeholder="Instance class...">
                                </div>
                                <div class="note">
                                    <i class="icon-warning-sign"></i> Warning: This is the training text area! You overwrite the original training model when you use this.
                                </div>
                                <div class="input">
                                    <textarea
                                        placeholder="Training data in Weka format..."
                                        rows="5"
                                        ng-model="analysis.input"
                                        title="positive,'Fantastic holiday'
negative,'Terrible weather'
positive,'my beautiful little girl'
negative,'you selfish stupid woman'"></textarea>
                                </div>
                                <button type="submit" class="button blue" style="margin-top: 5px;" ng-click="doTrain()">Train</button>
                                <button type="submit" class="button blue" style="margin-top: 5px;" ng-click="doBuild()">Build</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>