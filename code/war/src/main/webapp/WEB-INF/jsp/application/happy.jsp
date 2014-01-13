<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="container-fluid" ng-controller="HappyController">
    <form>
        <div class="row-fluid">
            <div class="span12">
                <div class="box tex">
                    <div class="tab-header">Geo tweets</div>
                    <div id="geo_chart_div" style="width: 100%; height: 500px;"></div>
                </div>
            </div>
        </div>
    </form>
</div>