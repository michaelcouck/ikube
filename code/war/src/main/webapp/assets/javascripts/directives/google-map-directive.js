var style = {
	width : '450px',
	height : '300px',
	border : '1px solid black'
};

/** This directive will just init the map and put it on the page. */
module.directive('googleMap', function($window, $log) {
	return {
		restrict : 'ECMA',
		compile : function($scope, $element, $attributes, $transclude) {
			
			return function($scope, $element, $attributes) {
				$scope.doMap = function() {
					var parent = angular.element($element).parent();
					$scope.styleMap = function() {
						style.width = (parent.width() - 70) + 'px';
						$element.css(style);
						$log.log('Do map : ' + style.width + ', ' + $element.width() + ', ' + parent.width());
						return style;
					};
					
					// The initial coordinates are for Cape Town
					var mapElement = document.getElementById('map_canvas');
					var latitude = -33.9693580;
					var longitude = 18.4622110;
					var options = {
						zoom: 13,
						center: new google.maps.LatLng(latitude, longitude),
						mapTypeId: google.maps.MapTypeId.ROADMAP
					};
					map = new google.maps.Map(mapElement, options);
					$scope.styleMap();
				};
				
				angular.element($window).bind('resize', function() {
					$scope.doMap();
				});
				$scope.doMap();
			}
		}
	};
});