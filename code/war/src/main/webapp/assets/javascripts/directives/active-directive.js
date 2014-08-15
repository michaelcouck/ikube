/**
 * This directive is for setting the link(buttons) to be active, and the sub-menu
 * buttons and links to be active. The path of the current document location is checked
 * against the href in the same html tag, and a class, 'active', is set when there is
 * a match in the path.
 *
 * @author Michael Couck
 * @since 24-11-2013
 */
module.directive('activeLink', [ '$location', function(location) {
	return {
		restrict : 'A',
		link : function(scope, element, attrs, controller) {
            // This is the class that we will set in the style attribute
			var clazz = attrs.activeLink;
            // This is the part of the url that we will try to match
			var href = attrs.href;
			// Hack because path does not return including hash bang
			href = href.substring(1);
            // The location of the document, i.e. the url
			scope.location = location;
			scope.$watch('location.absUrl()', function(newPath) {
				var parts = href.split(',');
                // See if the href property is in the url, for example if 'system'
                // is the href property, and /system/dash.html is the url then we have
                // a match and the style attribute is set to active
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