/**
 * This service is just to be injected into controllers that want to post a notification using the Bootstrap/Plastique notification service.
 * 
 * @author Michael Couck
 * @since 15-11-13
 */
ikubeServices.service('notificationService', function($rootScope) {

	this.notification = function(text, image, duration) {
		var imagePath = getServiceUrl(image);
		new Notifications({
			container : $("body"),
			bootstrapPositionClass : "span8 offset2"
		});
		return Notifications.push({
			imagePath : imagePath,
			text : text,
			autoDismiss : duration
		});
	};

});