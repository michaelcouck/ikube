/**
 * @author Michael Couck
 * @since 13-01-2014
 */
module.controller('HappyController', function ($scope, $http, $injector, $timeout, $log) {

    $scope.analyzing = false;
    $scope.status = 200;
    $scope.searchUrl = '/ikube/service/twitter/happy';

    $scope.search = {
        fragment: false,
        firstResult: 0,
        maxResults: 100000,
        indexName: 'twitter',
        minutesOfHistory: 600,
        classification: 'positive',
        clusters: 100,
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
        });
        promise.error(function (data, status) {
            $scope.analyzing = false;
            $scope.status = status;
            $log.log('Error in doHappySearch : ' + status);
        });
    };

    $scope.drawHeatMap = function (search) {
        // Construct the lat/long from the heat map data
        var mapElement = document.getElementById('map_canvas');
        var options = {
            zoom: 2,
            center: new google.maps.LatLng(0.0, 0.0),
            mapTypeId: google.maps.MapTypeId.ROADMAP
        };
        var map = new google.maps.Map(mapElement, options);

        var heatMapData = [
            {location: new google.maps.LatLng(37.782, -122.447), weight: 1.0}
        ];
        $log.log('Heat map data : ' + search.heatMapData);
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

        heatmap.set('radius', 30);
        heatmap.set('opacity', 10);
        heatmap.setMap(map);
    };

    $scope.setGradient = function (heatmap) {
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
            'rgba(127, 0, 63, 1)',
            'rgba(191, 0, 31, 1)',
            'rgba(255, 0, 0, 1)'
        ]
        heatmap.set('gradient', gradient);
    }

    // This function will put the markers on the map
    $scope.doMarkers = function (search) {
        return $timeout(function () {
            if (!!search.searchResults) {
                $log.log('Showing markers : ');
                angular.forEach(search.searchResults, function (key, value) {
                    var latitude = key['latitude'];
                    var longitude = key['longitude'];
                    var fragment = key['fragment'];
                    var distance = key['distance'];
                    if (!!latitude && !!longitude) {
                        $log.log('       marker : ' + latitude + '-' + longitude);
                        new google.maps.Marker({
                            map: $scope.map,
                            icon: getServiceUrl('/ikube/assets/images/icons/person_obj.gif'),
                            position: new google.maps.LatLng(latitude, longitude),
                            title: 'Name : ' + fragment + ', distance : ' + distance
                        });
                    }
                });
            }
        }, 100);
    };

    google.setOnLoadCallback($scope.drawGeoMap);
    setInterval(function () {
        // $scope.doHappySearch();
    }, 60000);
    $scope.doHappySearch();
    // google.setOnLoadCallback($scope.drawGeoChart);

});