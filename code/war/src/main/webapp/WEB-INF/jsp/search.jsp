<table ng-controller="SearcherController" class="table table-condensed"">
	<tr>
		<td>
			<form ng-submit="doSearch()">
			<table>
				<!-- <tr>
					<td colspan="5">
						<a href="#" 
							class="btn btn-small : hover" 
							ng-click="isConfiguring = !isConfiguring">Configuration</a>
						<div ng-show="isConfiguring">
							Configuration:
						</div>
					</td>
				</tr> -->
				<tr>
					<td style="border: 0px solid black;"><b>Collection:</b></td>
					<td colspan="4" nowrap="nowrap" style="border: 0px solid black;">
						<select 
							ng-controller="IndexesController" 
							ng-model="indexName" 
							ng-model="indexes" 
							ng-options="index for index in indexes" 
							ng-change="resetSearch();doFields(indexName);setField('indexName', indexName);">
							<option style="display:none" value="">collection</option>
						</select>
						<select ng-model="pageBlock">
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
					<td width="180px"><b>Fields:</b></td>
					<td colspan="4">
						<select 
							ng-model="field" 
							ng-model="fields" 
							ng-options="field for field in fields" 
							ng-change="
								pushField('searchFields', field); 
								pushField('typeFields', 'string');
								setField('firstResult', 0);">
							<option style="display:none" value="">field</option>
						</select>
					</td>
				</tr>
				<tr ng-repeat="field in search.searchFields" ng-hide="search.searchFields == undefined || search.searchFields == null || search.searchFields.length == 0">
					<td><b>{{field}}:</b></td>
					<td colspan="3"><input id="{{field}}" name="{{field}}" ng-model="search.searchStrings[$index]"></td>
					<td>
						<a href="#" 
							class="btn btn-small btn-success : hover" 
							ng-click="
								removeField('searchFields', $index); 
								removeField('searchStrings', $index); 
								removeField('typeFields', $index);">Remove</a>
					</td>
				</tr>
				<tr>
					<td colspan="5">
						<button type="submit" class="btn btn-warning : hover">Go!</button>
					</td>
				</tr>
			</table>
			</form>
		</td>
		<td
			ng-show="
				search.coordinate != undefined && 
				search.coordinate != null && 
				search.coordinate.latitude != 0 && 
				search.coordinate.longitude != 0">
			<div id="map_canvas" google-map style="height: 340px; width: 550px; border : 2px solid black;" ></div>
		</td>
	</tr>
	
	<tr ng-show="statistics != undefined && statistics">
		<td colspan="2">
			Showing results '{{search.firstResult}} to {{endResult}} of {{statistics.total}}' for search '{{search.searchStrings}}', duration : {{statistics.duration}}<br>
			<!-- Sorting by {{predicate}}; reverse = {{reverse}}
			<select 
				ng-model="predicate"
				ng-model="fields" 
				ng-options="field for field in fields">
				<option style="display:none" value="">sort</option>
			</select> -->
			<div ng-show="statistics != undefined && statistics.corrections != undefined && statistics.corrections.length > 0">
				<a href="#" ng-click="setField('searchStrings', statistics.corrections);doSearch();">Did you mean : {{statistics.corrections}}</a>
			</div>
			<div class="row">
	      		<div class="span6">
					<div class="pagination">
						<ul>
							<li ng-repeat="page in pagination">
								<a href="#" ng-click="setField('firstResult', page.firstResult);doSearch();">{{page.page}}</a>
							</li>
						</ul>
					</div>
				</div>
			</div>
		</td>
	</tr>
	
	<tr ng-repeat="result in search.searchResults" ng-show="search.searchResults != undefined && endResult > 0">
		<td colspan="2" nowrap="nowrap">
			<div ng-repeat="(field, value) in result | orderBy:predicate:reverse">{{field}} : {{value}}</div>
		</td>
	</tr>
	
	<tr ng-show="endResult > 0">
		<td colspan="2">
			<div class="row">
	      		<div class="span6">
					<div class="pagination">
						<ul>
							<li ng-repeat="page in pagination">
								<a href="#" ng-click="setField('firstResult', page.firstResult);doSearch();">{{page.page}}</a>
							</li>
						</ul>
					</div>
				</div>
			</div>
		</td>
	</tr>
</table>
