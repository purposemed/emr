
<div ng-class="{ 'input-group': hasButtons() } ">

	<input ng-if="hasTemplateUrl()"
				 type="text"
				 class="form-control"
				 autocomplete="off"
				 spellcheck="false"
				 ng-model="$parent.searchField"
				 ng-model-options="typeaheadModelOptions"
				 ng-change="onChange()"
				 ng-blur="onBlur()"
				 uib-typeahead="match for match in findMatches($viewValue)"
				 typeahead-template-url="{{ optionsTemplateUrl }}"
				 typeahead-input-formatter="formatMatch($model)"
				 typeahead-on-select="onSelect($item, $model, $label, $event)"
				 typeahead-min-length="1"
				 typeahead-editable="true"
				 typeahead-select-on-blur="false"
				 typeahead-focus-first="false"
				 typeahead-append-to-body="false"
				 placeholder="{{placeholder}}"/>

	<input ng-if="!hasTemplateUrl()"
				 type="text"
				 class="form-control"
				 autocomplete="off"
				 spellcheck="false"
				 ng-model="$parent.searchField"
				 ng-model-options="typeaheadModelOptions"
				 ng-change="onChange()"
				 ng-blur="onBlur()"
				 uib-typeahead="match as typeaheadLabel(match) for match in findMatches($viewValue)"
				 typeahead-input-formatter="formatMatch($model)"
				 typeahead-on-select="onSelect($item, $model, $label, $event)"
				 typeahead-min-length="1"
				 typeahead-editable="true"
				 typeahead-select-on-blur="false"
				 typeahead-focus-first="false"
				 typeahead-append-to-body="false"
				 placeholder="{{placeholder}}"/>

	<span ng-if="hasButtons()"
				class="input-group-btn">
		<button ng-if="hasSearchButton()"
			type="button"
			class="btn btn-default btn-search"
			ng-click="onSearch()"
			title="{{searchButtonTitle}}">
			<span class="glyphicon glyphicon-search" ></span>
		</button>

		<button ng-if="hasAddButton()" 
			type="button"
			class="btn btn-default" 
			ng-click="onAdd()"
			title="{{addButtonTitle}}">
			<span class="glyphicon glyphicon-plus"></span>
		</button>
	</span>


</div>