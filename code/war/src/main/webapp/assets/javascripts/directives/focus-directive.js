/**
 * This directive will just focus on the element where the attribute is found. Provided
 * of course that the expression that is evaluated results in a 'true' value.
 */
module.directive('focusMe', function($timeout) {
    return function(scope, element, attrs) {
        attrs.$observe('focusMe', function(value) {
            if ( value==="true" ) {
                $timeout(function(){
                    element[0].focus();
                },5);
            }
        });
    }
});