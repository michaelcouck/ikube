/**
 * @author Michael Couck
 * @since 29-01-2014
 *
 * @param $scope
 * @param $http
 * @param $injector
 * @param $timeout
 */
module.controller('FileUploadController', function ($http, $scope, $injector, $timeout, notificationService) {

    // The file to upload
    $scope.file = undefined;

    // Listen for the file selected event
    $scope.$on("fileSelected", function (event, args) {
        $scope.$apply(function () {
            // Add the file object to the scope's file
            $scope.file = args.file;
        });
    });

    // the save method
    $scope.upload = function () {
        var url = getServiceUrl('/ikube/service/analyzer/upload');
        var fd = new FormData();
        fd.append('file', $scope.file);
        $http.post(url, fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        }).success(function () {
            var text = ['Uploaded file : ', $scope.file, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/green_tick.png', 5);
        }).error(function () {
            var text = ['Failed to uploaded file : ', $scope.file, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/red_cross.png', 5);
        });
    };

});