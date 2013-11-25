<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="container-fluid" ng-controller="AnalyticsController">
	<div class="row-fluid">
		<div class="span12">
			<div class="box">
				<div class="tab-header">
					<b>Analytics<b>
					<img 
						ng-show="!!status" 
						alt="Loading spinner" 
						src="<c:url value="/assets/images/loading.gif" />" 
						height="16px" 
						width="16px" >
					<span class="pull-right">
						<span class="options">
							<a href="#"><i class="icon-cog"></i></a>
						</span>
					</span>
				</div>
				
				<form class="fill-up">
					<div class="row-fluid">
						<div class="span6">
							<div class="padded">
								<form ng-submit="doAnalysis()">
								Analysis
								<div class="note pull-right"><b>Select an analyzer</b></div>
								<div class="input search">
									<select 
										ng-model="analysis.analyzer"
										ng-model="analyzers"
										ng-options="analyzer for analyzer in analyzers"
										class="fill-up">
										<option style="display:none" value="">Analyzer...</option>
									</select>
								</div>
								<div class="note large">
									<i class="icon-pencil"></i> 
									Note: The first attribute is the class attribute, which you then omit in the 
									case of a classifier. With clusterers there is no class attribute.
								</div>
								<div class="input">
									<textarea 
										placeholder="Input data in Weka format...(essentially csv format)" 
										rows="8"
										ng-model="analysis.input" 
										title="datum,datum,datum...
datum,datum,datum..."></textarea>
								</div>
								<div class="input search">
									<input type="file" >
								</div>
								<button type="submit" class="button blue" ng-disabled="!analysis.input" ng-click="doAnalysis();">Go</button>
								</form>
							</div>
						</div>
						
						<div class="span6">
							<div class="padded">
								<form ng-submit="doTrain()">
								<b>Analyzer training</b>
								<div class="note pull-right"><b>Specify analyzer unique identifier</b></div>
								<div class="input">
									<!-- ng-model="analysis.analyzer" -->
									<input
										id="analyzer-identifier"
										name="analyzer-identifier" 
										type="text"
										class="search"
										focus-me="true"
										placeholder="Unique identifier...">
									<div class="input search pull-right" ng-show="true">
										Identifier already in use : 
										<a href="#" ng-click="">classifier-em</a>
									</div>
								</div><br>
								<div class="note large">
									<i class="icon-warning-sign"></i> Warning: This is the training text area!
								</div>
								<div class="input">
									<textarea 
										placeholder="Training data in Weka format..." 
										rows="12"
										ng-model="analysis.buildable.trainingData"
										title="@relation sentiment
@attribute class {positive,negative}
@attribute text String
@data
positive,'Fantastic holiday'
negative,'Terrible weather'
positive,'positive'
negative,'negative'
positive,'my beautiful little girl'
negative,'you selfish stupid woman'"></textarea>
								</div>
								<div class="input search">
									<input type="file" >
								</div>
								<button type="submit" class="button blue" ng-disabled="!searchString" ng-click="doSearchAll([searchString]);">Go</button>
								</form>
							</div>
						</div>
					</div>
				</form>
			</div>
		</div>
	</div>
</div>

<div class="container-fluid">
	<div class="row-fluid">
		<div class="span6">
			<div class="box padded">
				<div id="chart1" style="width: 100%; height: 250px;"></div>
			</div>
		</div>
		<div class="span6">
			<div class="box padded">
				<div id="chart2" style="width: 100%; height: 250px;"></div>
			</div>
		</div>
	</div>

	<div class="row-fluid">
		<div class="span6">
			<div class="box padded">
				<div id="chart3" style="width: 100%; height: 250px;"></div>
			</div>
		</div>
		<div class="span6">
			<div class="box padded">
				<div id="chart4" style="width: 100%; height: 250px;"></div>
			</div>
		</div>
	</div>