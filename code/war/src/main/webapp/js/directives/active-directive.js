module.directive('activeLink', [ '$location', function(location) {
	return {
		restrict : 'A',
		link : function(scope, element, attrs, controller) {
			var clazz = attrs.activeLink;
			var path = attrs.href;
			// Hack because path does bot return including hash bang
			path = path.substring(1);
			scope.location = location;
			scope.$watch('location.absUrl()', function(newPath) {
				var parts = path.split(',');
				for (var i = 0; i < parts.length; i++) {
					if (newPath.indexOf(parts[i]) > -1) {
						element.addClass(clazz);
					} else {
						element.removeClass(clazz);
					}
				}
			});
		}

	};
} ]);