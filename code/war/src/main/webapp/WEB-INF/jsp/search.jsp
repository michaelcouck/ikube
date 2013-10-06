<table ng-controller="SearcherController" class="table table-condensed"">
	<tr>
		<td>
			<form ng-submit="doSearch()">
			<table border="0">
				<tr>
					<td><b>Collection:</b></td>
					<td colspan="4">
						<select ng-controller="IndexesController" ng-model="indexName" ng-change="addFields(indexName);setIndex(indexName);">
							<option ng-repeat="index in indexes" value="{{index}}">{{index}}</option>
						</select>
					</td>
				</tr>
				<tr>
					<td><b>Fields:</b></td>
					<td colspan="4">
						<select ng-model="field" ng-model="fields" ng-options="field for field in fields" ng-change="addField(field)">
							<option style="display:none" value="">select a field</option>
						</select>
					</td>
				</tr>
				<!-- ng-model="search.searchFields" -->
				<tr ng-repeat="field in search.searchFields" ng-show="search.searchFields">
					<td><b>{{field}}</b></td>
					<td><input id="{{field}}" name="{{field}}" ng-model="searchString" ng-change="addSearchString($index, searchString)"></td>
					<td>Numeric:<input type="checkbox" ng-model="sortField" ng-change="addNumericField($index);"></td>
					<td>Sort:<input type="checkbox" ng-model="sortField" ng-change="addSortField($index);"></td>
					<td>
						<button class="btn btn-small btn-success : hover" ng-click="removeField($index);removeSearchString($index);">Remove</button>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<!-- ng-click="doSearch()" -->
						<button 
							type="submit" 
							class="btn btn-warning : hover">Go!</button>
					</td>
				</tr>
			</table>
			</form>
		</td>
		<td>
			Map here if required
		</td>
	</tr>
	
	<tr ng-show="search.indexName" >
		<td colspan="2">
			Showing results '{{search.firstResult}} 
			to {{endResult}} 
			of {{statistics.total}}' 
			for search '{{search.searchStrings}}', 
			corrections : {{statistics.corrections}}, 
			duration : {{statistics.duration}}</td>
	</tr>
	
	<tr ng-show="pagination">
		<td colspan="2">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" ng-click="
					doFirstResult(page.firstResult);
					doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr ng-repeat="datum in data" ng-show="pagination">
		<td colspan="2">
			<span ng-hide="!datum.id"><b>Identifier</b> : {{datum.id}}<br></span> 
			<b>Score</b> : {{datum.score}}<br>
			<b>Fragment</b> : <span ng-bind-html-unsafe="datum.fragment"></span><br>
			<br>
		</td>
	</tr>
	
	<tr>
		<td ng-show="pagination" colspan="2">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" ng-click="doFirstResult(page.firstResult);doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
</table>