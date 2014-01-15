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
                        var style = {};
                        var width = 0;
                        var height = 0;
                        // We need this because none of the parents might have
						// no size which creates an infinite iteration
						var count = 100;
						do {
							if (parent.width() > 0 && width == 0) {
                                width = parent.width();
                            }
							if (parent.height() > 0 && height == 0) {
                                height = parent.height();
                            }
                            parent = parent.parent();
                        } while (!!parent && count-- > 0);

                        style.width = width + 'px';
                        style.height = height + 'px';

						$element.css(style);
						// $log.log('Width : ' + style.width + ', element : ' + $element.width());
						// $log.log('Height : ' + style.height + ', element : ' + $element.height());
						return style;
					};

					// The initial coordinates are for Cape Town
					var mapElement = document.getElementById('map_canvas');
					var latitude = 0.0;
					var longitude = 0.0;
					var options = {
						zoom: 2,
						center: new google.maps.LatLng(latitude, longitude),
						mapTypeId: google.maps.MapTypeId.ROADMAP
					};
					var map = new google.maps.Map(mapElement, options);
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