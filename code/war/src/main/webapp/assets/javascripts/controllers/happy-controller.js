/**
 * @author Michael Couck
 * @since 13-01-2014
 */
module.controller('HappyController', function ($scope, $http, $timeout, $log) {

    $scope.status = 200;
    $scope.map = undefined;
    $scope.analyzing = false;
    $scope.searchUrl = '/ikube/service/twitter/happy';

    $scope.search = {
        fragment: false,
        firstResult: 0,
        maxResults: 100000,
        indexName: 'twitter',
        minutesOfHistory: 600,
        classification: 'positive',
        clusters: 1000,
        heatMapData: [],
        searchStrings: [],
        searchFields: [],
        occurrenceFields: [],
        typeFields: []
    };

    $scope.doHappySearch = function () {
        if ($scope.analyzing) {
            return;
        }
        $scope.analyzing = true;
        var url = getServiceUrl($scope.searchUrl);
        var promise = $http.post(url, $scope.search);
        promise.success(function (data, status) {
            $scope.analyzing = false;
            $scope.status = status;
            $scope.drawHeatMap(data);
            $scope.drawMarkers(data);
        });
        promise.error(function (data, status) {
            $scope.analyzing = false;
            $scope.status = status;
            $log.log('Error in doHappySearch : ' + status);
        });
    };

    $scope.drawHeatMap = function (search) {
        // Construct the lat/long from the heat map data
        var zoom;
        var origin;
        if (!!$scope.map) {
            zoom = $scope.map.getZoom();
            origin = $scope.map.getCenter();
        } else {
            zoom = 2;
            origin = new google.maps.LatLng(0.0, 0.0);
        }
        var options = {
            zoom: zoom,
            center: origin,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        };

        var mapElement = document.getElementById('map_canvas');
        $scope.map = new google.maps.Map(mapElement, options);

        var heatMapData = [];
        // $log.log('Heat map data : ' + search.heatMapData);
        angular.forEach(search.heatMapData, function (value) {
            var latitude = value[0];
            var longitude = value[1];
            var weight = value[2];
            // $log.log('Lat : ' + latitude + ', long : ' + longitude + ', weight : ' + weight);
            heatMapData.push({location: new google.maps.LatLng(latitude, longitude), weight: weight});
        });
        var pointArray = new google.maps.MVCArray(heatMapData);
        var heatmap = new google.maps.visualization.HeatmapLayer({
            data: pointArray
        });

        heatmap.set('radius', 10);
        heatmap.set('opacity', 10);
        $scope.setGradient(heatmap);
        heatmap.setMap($scope.map);
    };

    $scope.setGradient = function (heatmap) {
        /*var gradient = [
         'rgba(15, 240, 27, 0)',
         'rgba(32, 239, 32, 1)',
         'rgba(49, 239, 37, 1)',
         'rgba(72, 239, 32, 1)',
         'rgba(84, 239, 47, 1)',
         'rgba(101, 239, 52, 1)',
         'rgba(118, 239, 57, 1)',
         'rgba(136, 239, 62, 1)',
         'rgba(153, 239, 67, 1)',
         'rgba(170, 239, 72, 1)',
         'rgba(188, 239, 77, 1)',
         'rgba(205, 239, 82, 1)',
         'rgba(222, 239, 87, 1)',
         'rgba(240, 239, 92, 1)'
         ];*/
        var gradient = [
            'rgba(0, 255, 255, 0)',
            'rgba(0, 255, 255, 1)',
            'rgba(0, 191, 255, 1)',
            'rgba(0, 127, 255, 1)',
            'rgba(0, 63, 255, 1)',
            'rgba(0, 0, 255, 1)',
            'rgba(0, 0, 223, 1)',
            'rgba(0, 0, 191, 1)',
            'rgba(0, 0, 159, 1)',
            'rgba(0, 0, 127, 1)',
            'rgba(63, 0, 91, 1)',
            'rgba(63, 0, 141, 1)'
            // 'rgba(191, 0, 31, 1)'
            // 'rgba(255, 0, 0, 1)'
        ];
        heatmap.set('gradient', gradient);
    }

    // This function will put the markers on the map
    $scope.drawMarkers = function (search) {
        var onScreen = 1000;
        var incrementSleep = 10;
        var sleep = incrementSleep;
        angular.forEach(search.searchResults, function (key, value) {
            if ($scope.analyzing) {
                return false;
            }
            ;
            sleep += incrementSleep;
            var latitude = key['latitude'];
            var longitude = key['longitude'];
            if (!!latitude || !!longitude) {
                // var distance = key['distance'];
                $timeout(function () {
                    var marker = new google.maps.Marker({
                        map: $scope.map,
                        icon: getServiceUrl('/ikube/assets/images/icons/person_obj.gif'),
                        position: new google.maps.LatLng(latitude, longitude),
                        title: 'Bla...'
                    });
                    // $log.log('Drawing marker : ' + latitude + '-' + longitude);
                    $timeout(function () {
                        // $log.log('Removing marker : ' + marker);
                        marker.setMap(null);
                    }, onScreen);
                }, sleep);
            }
        });
    };

    google.setOnLoadCallback($scope.drawGeoMap);
    setInterval(function () {
        $scope.doHappySearch();
    }, 60000);
    $scope.doHappySearch();
    // google.setOnLoadCallback($scope.drawGeoChart);

});