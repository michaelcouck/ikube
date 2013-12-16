/**
 * This directive will just focus on the element where the attribute is found.
 * Provided of course that the expression that is evaluated results in a 'true'
 * value.
 */
module.directive('focusMe', function($timeout) {
	return function(scope, element, attrs) {
		attrs.$observe('focusMe', function(value) {
			if (value === "true") {
				$timeout(function() {
					element[0].focus();
				}, 5);
			}
		});
	}
});

module.directive('ngFocus', [ '$parse', function($parse) {
	return function(scope, element, attr) {
		var fn = $parse(attr['ngFocus']);
		element.bind('focus', function(event) {
			scope.$apply(function() {
				fn(scope, {
					$event : event
				});
			});
		});
	}
} ]);