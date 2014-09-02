<div class="user-info" style="font-size: 12px;">
    {{apiMethod.description}}
</div>
<br>

<div class="user-content">

    Consumes: {{apiMethod.consumesType}}<br><br>
    <span ng-show="!!apiMethod.consumes">
        <textarea ng-disabled="true" disabled style="width: 100%;" rows="10">
            {{apiMethod.consumes}}
        </textarea>
        <br>
    </span>

    Produces: {{apiMethod.producesType}}<br><br>
    <span ng-show="!!apiMethod.produces">
        <textarea ng-disabled="true" disabled style="width: 100%;" rows="10">
            {{apiMethod.produces}}
        </textarea>
    </span>
</div>