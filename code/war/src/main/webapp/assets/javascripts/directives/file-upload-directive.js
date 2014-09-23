/**
 * @author Michael Couck
 * @since 23-09-2014
 */
module.directive('fileUpload', function () {
    return {
        scope: true,        //create a new scope
        link: function (scope, el, attrs) {
            el.bind('change', function (event) {
                var files = event.target.files;
                for (var i = 0;i<files.length;i++) {
                    scope.$emit("fileSelected", { file: files[i] });
                }                                       
            });
        }
    };
});