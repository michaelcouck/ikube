/**
 * This directive can be added to a textarea that is bound to a model of strings, like a list 
 * or a map. Generally when the text in the textarea is changed the model is revaluated and the 
 * text area loses focus, but this directive will intercept the event and not allow the model to 
 * be evaluated until the 'real' focus is lost when the users clicks outside the area. 
 */
module.directive('updateModelOnBlur', function() {
	return {
		restrict : 'A',
		require : 'ngModel',
		link : function(scope, elm, attr, ngModelCtrl) {
			if (attr.type === 'radio' || attr.type === 'checkbox') {
				return;
			}
			// Update model on blur only
			elm.unbind('input').unbind('keydown').unbind('change');
			var updateModel = function() {
				scope.$apply(function() {
					ngModelCtrl.$setViewValue(elm.val());
				});
			};
			elm.bind('blur', updateModel);
			// Not a textarea
			if (elm[0].nodeName.toLowerCase() !== 'textarea') {
				// Update model on ENTER
				elm.bind('keydown', function(e) {
					e.which == 13 && updateModel();
				});
			}
		}
	};
});