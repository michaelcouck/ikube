<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="container-fluid" ng-controller="ApisController">
    <div class="row-fluid">
        <div class="span12">
            <div class="row-fluid">
                <div class="big-button-bar">
                    <a class="button large" href="#" ng-repeat="api in apis"
                       ng-click="
                            toggleVisibilityAll(false);
                            setApi(api);">
                        <i class="icon-list-ul"></i><span>{{api.api}}</span></a>
                </div>
            </div>
        </div>
    </div>

    <div class="row-fluid" ng-show="!!api">
        <div class="span6">
            <div class="nav-menu box">
                <ul class="nav nav-list">
                    <li class="active">
                        <a href="#"><i class="icon-share-alt"></i>Rest Web Service</a>
                    </li>
                    <li>
                        <a href="#" ng-click="toggleVisibility(api.api)">
                            <i class="icon-info-sign"></i>{{api.api}}<br>
                        </a>
                        <span class="note" style="font-size: 12px; line-height: 13px;">{{api.description}}</span>

                        <span ng-show="visible[api.api]">
                            <br>
                            Methods exposed:
                            <br>
                            <ul class="nav nav-list"
                                ng-repeat="elementApiMethod in api.apiMethods">
                                <a href="#" ng-click="
                                 toggleVisibility(apiMethod.uri);
                                 toggleVisibility(elementApiMethod.uri);
                                 setApiMethod(elementApiMethod);">
                                    <li><i class="icon-screenshot"></i>
                                        <span style="line-height: 25px;">{{elementApiMethod.method}} : /ikube/{{elementApiMethod.uri}}</span>
                                    </li>
                                </a>
                            </ul>
                        </span>
                    </li>
                </ul>
            </div>
        </div>

        <div class="span6" ng-show="visible[apiMethod.uri]">
            <div class="box">
                <div class="tab-header">
                    <span style="font-size: 11px;">{{apiMethod.method}} : {{apiMethod.uri}}</span>
                </div>
                <div class="padded">
                    <jsp:include page="api.jsp"/>
                </div>
            </div>
        </div>

    </div>
</div>