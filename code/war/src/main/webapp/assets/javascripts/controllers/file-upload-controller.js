/**
 * @author Michael Couck
 * @since 29-01-2014
 *
 * @param $scope
 * @param $http
 * @param notificationsService
 */
module.controller('FileUploadController', function ($scope, $http, notificationService) {

    // Listen for the file selected event
    $scope.$on("fileSelected", function (event, args) {
        $scope.$apply(function () {
            // Add the file object to the scope's file
            $scope.file = args.file;
            $scope.doUpload(args.file, '/ikube/service/analyzer/upload');
        });
    });

    // the save method
    $scope.doUpload = function (file, uri) {
        var url = getServiceUrl(uri);
        var fd = new FormData();
        fd.append('file', file);
        $http.post(url, fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        }).success(function (data, status) {
            var text = ['Uploaded file : ', JSON.stringify($scope.file, null, 2), ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/green_tick.png', 5);
            $scope.$emit('fileUploaded', {file : file, status : status});
        }).error(function (data, status) {
            var text = ['Failed to uploaded file : ', JSON.stringify($scope.file, null, 2), ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/red_cross.png', 5);
            $scope.$emit('fileNotUploaded', {file : file, status : status});
        });
    };

});