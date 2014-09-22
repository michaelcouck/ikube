/**
 * @author Michael Couck
 * @since 29-01-2014
 *
 * @param $scope
 * @param $http
 * @param $injector
 * @param $timeout
 */
module.directive('fileUpload', function () {
    return {
        scope: true,        //create a new scope
        link: function (scope, el, attrs) {
            el.bind('change', function (event) {
                var files = event.target.files;
                //iterate files since 'multiple' may be specified on the element
                for (var i = 0;i<files.length;i++) {
                    //emit event upward
                    // alert('Emitting');
                    scope.$emit("fileSelected", { file: files[i] });
                }                                       
            });
        }
    };
});