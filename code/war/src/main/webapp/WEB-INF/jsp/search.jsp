<table ng-controller="SearcherController" class="table table-condensed"">
	<tr>
		<td>
			<form ng-submit="doSearch()">
			<table>
				<tr>
					<td><b>Collection:</b></td>
					<td colspan="4" nowrap="nowrap">
						<select 
							ng-controller="IndexesController" 
							ng-model="indexName" 
							ng-model="indexes" 
							ng-options="index for index in indexes" 
							ng-change="addFields(indexName);setIndex(indexName);">
							<option style="display:none" value="">collection</option>
							<!-- <option ng-repeat="index in indexes" value="{{index}}">{{index}}</option> -->
						</select>
						<!-- setMaxResults(10); -->
						<!-- ng-change="setMaxResults(maxResults);" -->
						<select ng-model="maxResults;" ng-init="maxResults=10;" ng-change="setMaxResults(maxResults);" ng-required>
							<!-- <option value="{{search.maxResults}}" ng-selected="selected" selected="selected">{{search.maxResults}}</option> -->
							<option value="10" >10</option>
							<option value="25">25</option>
							<option value="50">50</option>
							<option value="100">100</option>
							<option value="1000">1000</option>
							<option value="10000">stupid</option>
						</select>
					</td>
				</tr>
				<tr>
					<td><b>Fields:</b></td>
					<td colspan="4">
						<select ng-model="field" ng-model="fields" ng-options="field for field in fields" ng-change="addField(field);">
							<option style="display:none" value="">field</option>
						</select>
					</td>
				</tr>
				<tr ng-repeat="field in search.searchFields" ng-show="search.searchFields">
					<td><b>{{field}}</b></td>
					<td><input id="{{field}}" name="{{field}}" ng-model="searchString" ng-change="addSearchString($index, searchString)"></td>
					<td>Numeric : <input type="checkbox" ng-model="sortField" ng-change="addNumericField($index);"></td>
					<td>Sort : <input type="checkbox" ng-model="sortField" ng-change="addSortField($index);"></td>
					<td><a href="#" class="btn btn-small btn-success : hover" ng-click="removeField($index);removeSearchString($index);">Remove</a></td>
				</tr>
				<tr>
					<td colspan="5">
						<button type="submit" class="btn btn-warning : hover">Go!</button>
					</td>
				</tr>
			</table>
			</form>
		</td>
		<td>
			<table>
				<tr>
					<td>Map</td>
				</tr>
			</table>
		</td>
	</tr>
	
	<tr ng-show="statistics != undefined && statistics">
		<td colspan="2">
			Showing results '{{search.firstResult}} to {{endResult}} of {{statistics.total}}' for search '{{search.searchStrings}}', duration : {{statistics.duration}}<br>
			<div ng-show="statistics != undefined && statistics.corrections != undefined && statistics.corrections.length > 0">
				<a href="#" ng-model="searchStrings" ng-click="setSearchStrings(statistics.corrections);doSearch();">Did you mean : {{statistics.corrections}}</a>
			</div>
			<div class="row">
	      		<div class="span6">
					<div class="pagination">
						<ul>
							<li ng-repeat="page in pagination">
								<a href="#" ng-click="doFirstResult(page.firstResult);doSearch();">{{page.page}}</a>
							</li>
						</ul>
					</div>
				</div>
			</div>
		</td>
	</tr>
	
	<tr ng-repeat="result in search.searchResults" ng-show="search.searchResults != undefined && endResult > 0">
		<td colspan="2">
			<div ng-repeat="(field, value) in result">{{field}} : {{value}}</div>
		</td>
	</tr>
	
	<tr ng-show="endResult > 0">
		<td colspan="2">
			<div class="row">
	      		<div class="span6">
					<div class="pagination">
						<ul>
							<li ng-repeat="page in pagination">
								<a href="#" ng-click="doFirstResult(page.firstResult);doSearch();">{{page.page}}</a>
							</li>
						</ul>
					</div>
				</div>
			</div>
		</td>
	</tr>
	
</table>