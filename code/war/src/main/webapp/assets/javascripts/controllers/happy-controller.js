/**
 * This controller is for displaying the heat map for positive tweets. Please read method
 * documentation for more implementation details.
 *
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
        minutesOfHistory: 60,
        classification: 'positive',
        clusters: 1000,
        heatMapData: [],
        searchStrings: [],
        searchFields: [],
        occurrenceFields: [],
        typeFields: []
    };

    /**
     * This function will call the twitter rest web service, then display the heat map
     * clustered data, and additionally put all the tweets on the map one by one with a small
     * interval and then remove them.
     */
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

    /**
     * This function will convert the clustered heat map data from
     * an array of arrays to an array of location-lat/lng for display on the
     * map.
     *
     * @param search the search object that contains the heat map data clustered
     */
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
        angular.forEach(search.heatMapData, function (value) {
            var latitude = value[0];
            var longitude = value[1];
            var weight = value[2];
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

    /**
     * This function sets the colour and gradient of the heat map.
     *
     * @param heatmap the map to set the gradient for
     */
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
            'rgba(63, 0, 141, 1)'
        ];
        heatmap.set('gradient', gradient);
    }

    /**
     * This function will put the markers on the map one by one, with an interval oof a few
     * milliseconds. It will then remove the markers after a second to give the feeling that the
     * tweets are populating the map live.
     *
     * @param search the search object that contains the results and the tweets' positions
     */
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
            if (!!latitude && !!longitude) {
                $timeout(function () {
                    var marker = new google.maps.Marker({
                        map: $scope.map,
                        icon: getServiceUrl('/ikube/assets/images/icons/person_obj.gif'),
                        position: new google.maps.LatLng(latitude, longitude),
                        title: 'Bla...'
                    });
                    $timeout(function () {
                        marker.setMap(null);
                    }, onScreen);
                }, sleep);
            }
        });
    };

    // This acts like jQuery $(document).ready() i.e. when AngularJs is done
    // most of the work to initialize the page and the parsing etc.
    google.setOnLoadCallback($scope.drawGeoMap);
    setInterval(function () {
        $scope.doHappySearch();
    }, 60000);
    $scope.doHappySearch();

});