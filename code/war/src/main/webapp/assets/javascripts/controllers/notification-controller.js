/**
 * This controller is just a wrapper around some html to facilitate notifications.
 *
 * @author Michael Couck
 * @since 29-01-2014
 *
 * @param $scope
 * @param notificationService
 */
module.controller('NotificationController', function ($scope, notificationService) {

    $scope.enterpriseNotification = function () {
        notificationService.enterpriseNotification();
    };

});