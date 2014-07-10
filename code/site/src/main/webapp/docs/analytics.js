(function (i, s, o, g, r, a, m) {
    i['GoogleAnalyticsObject'] = r;
    i[r] = i[r] || function () {
        (i[r].q = i[r].q || []).push(arguments)
    }, i[r].l = 1 * new Date();
    a = s.createElement(o),
        m = s.getElementsByTagName(o)[0];
    a.async = 1;
    a.src = g;
    m.parentNode.insertBefore(a, m)
})(window, document, 'script', '//www.google-analytics.com/analytics.js', 'ga');

ga('create', 'UA-13044914-5', 'auto');
ga('send', 'pageview');

var module = angular.module('ikube', []);

/**
 * This controller will get the api details from the server, and make the
 * Json objects available for display.
 */
module.controller('ApisController', function($http, $scope) {

    $scope.apis = undefined;

    /**
     * Gets the api documentation from the server.
     */
    $scope.doApis = function() {
        $scope.url = 'http://ikube.be/ikube/service/api/apis';
        // $scope.url = getServiceUrl('http://ikube.be/ikube/service/api/apis');
        var promise = $http.get($scope.url);
        promise.success(function(data, status) {
            $scope.apis = data;
            $scope.status = status;
        });
        promise.error(function(data, status) {
            $scope.status = status;
        });
    };
    $scope.doApis();

});

function getServiceUrl(path) {
    var url = [];
    url.push(window.location.protocol);
    url.push('//');
    url.push(window.location.host);
    url.push(path);
    return url.join('');
}