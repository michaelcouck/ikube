<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="container-fluid" ng-controller="TwitterController" ng-init="doConfig('searchTwitterFormConfig');">
    <form>
        <div class="row-fluid">
            <div class="span12">
                <div class="box tex">
                    <div class="tab-header">Geo tweets</div>
                    <div id="geo_chart_div" style="width: 100%; height: 250px;"></div>
                </div>
            </div>
        </div>
    </form>
</div>