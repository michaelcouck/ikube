//noinspection JSUnusedLocalSymbols
/**
 * @author Michael Couck
 * @since 24-11-2013
 *
 * @param $scope
 * @param $http
 * @param $injector
 * @param $timeout
 */
module.controller('AnalyticsController', function ($http, $scope, $injector, $timeout, notificationService) {

    var textColour = "#aaaaaa";

    $scope.status = 200;

    $scope.origin = [0, 0];
    $scope.headers = ["Cluster", "Cluster probability"];

    $scope.analyzer = undefined;
    $scope.analyzers = undefined;
    $scope.contexts = undefined;

    $scope.analysis = {
        analyzer: undefined,
        clazz: undefined,
        input: undefined,
        output: undefined,

        algorithmOutput: true,

        correlation: false,
        distribution: false,
        classesAndClusters: false,
        sizesForClassesAndClusters: false
    };

    $scope.context = {
        name: undefined,
        options: undefined, // -N 6 (six clusters for example)
        trainingData: undefined,
        maxTraining: 10000,
        analyzerInfo : {
            analyzer : undefined,
            algorithm : undefined,
            filter : undefined
        }
    };

    $scope.doCreate = function () {
        var url = getServiceUrl('/ikube/service/analyzer/create');

        //noinspection JSUnresolvedVariable
        var context = angular.copy($scope.context);
        if (!!context.options) {
            context.options = context.options.split(',');
        }
        $scope.status = undefined;
        var promise = $http.post(url, context);
        promise.success(function (data, status) {
            $scope.status = status;
            $scope.doAnalyzers();
            var text = ['Created analyzer : ', $scope.context.name, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/green_tick.png', 5);
        });
        promise.error(function (data, status) {
            $scope.status = status;
            var text = ['Exception creating analyzer : ', $scope.context.name, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/red_cross.png', 15);
        });
    };

    $scope.doTrain = function () {
        var url = getServiceUrl('/ikube/service/analyzer/train');

        $scope.status = undefined;
        var promise = $http.post(url, $scope.analysis);
        promise.success(function (data, status) {
            $scope.status = status;
            var text = ['Trained analyzer : ', $scope.analysis.analyzer, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/green_tick.png', 5);
        });
        promise.error(function (data, status) {
            $scope.status = status;
            var text = ['Failed to train analyzer : ', $scope.analysis.analyzer, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/red_cross.png', 15);
        });
    };

    $scope.doBuild = function () {
        var url = getServiceUrl('/ikube/service/analyzer/build');

        $scope.status = undefined;
        var promise = $http.post(url, $scope.analysis);
        promise.success(function (data, status) {
            $scope.status = status;
            $scope.context = data;
            var text = ['Built analyzer : ', $scope.analysis.analyzer, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/green_tick.png', 5);
        });
        promise.error(function (data, status) {
            $scope.status = status;
            var text = ['Failed to build analyzer : ', $scope.analysis.analyzer, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/red_cross.png', 15);
        });
    };

    $scope.doAnalysis = function () {
        var url = getServiceUrl('/ikube/service/analyzer/analyze');
        //noinspection JSUnresolvedVariable

        $scope.analysis.clazz = null;
        $scope.analysis.output = null;
        $scope.analysis.exception = null;
        $scope.analysis.algorithmOutput = null;
        $scope.analysis.correlationCoefficients = false;
        $scope.analysis.distributionForInstances = false;

        $scope.analysis.algorithmOutput = true;
        $scope.analysis.correlation = false;
        $scope.analysis.distribution = false;
        $scope.analysis.classesAndClusters = false;
        $scope.analysis.sizesForClassesAndClusters = false;

        $scope.status = undefined;
        var promise = $http.post(url, $scope.analysis);
        promise.success(function (data, status) {
            $scope.status = status;
            $scope.analysis = data;
        });
        promise.error(function (data, status) {
            $scope.status = status;
        });
    };

    $scope.doAnalyzers = function () {
        var url = getServiceUrl('/ikube/service/analyzer/analyzers');
        var promise = $http.get(url);

        $scope.status = undefined;
        promise.success(function (data, status) {
            $scope.status = status;
            $scope.analyzers = data;
        });
        promise.error(function (data, status) {
            $scope.status = status;
        });
    };

    $scope.doContexts = function () {
        var url = getServiceUrl('/ikube/service/analyzer/contexts');
        var promise = $http.get(url);

        $scope.status = undefined;
        promise.success(function (data, status) {
            $scope.status = status;
            $scope.contexts = data;
        });
        promise.error(function (data, status) {
            $scope.status = status;
        });
    };

    $scope.doContext = function() {
        var url = getServiceUrl('/ikube/service/analyzer/context');

        $scope.status = undefined;
        var promise = $http.post(url, $scope.analysis);
        promise.success(function (data, status) {
            $scope.status = status;
            $scope.context = data;
        });
        promise.error(function (data, status) {
            $scope.status = status;
            var text = ['Couldn\'t get context for analyzer : ', $scope.context.name, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/red_cross.png', 15);
        });
    };

    $scope.doAnalyzers();
    $scope.doContexts();

    $scope.doChart = function (distributionForInstances) {
        //noinspection JSUnresolvedVariable,JSUnresolvedFunction
        return {
            type: "ScatterChart",
            cssStyle: "height:300px; width:100%;",
            data: google.visualization.arrayToDataTable(distributionForInstances),
            options: {
                legend: { position: 'bottom', textStyle: { color: '#aaaaaa', fontSize: 10 } },
                title: "Clustered instances",
                titleTextStyle: { fontSize: 16, color: '#999999' },
                vAxis: { title: 'Probability', titleTextStyle: { color: textColour, fontSize: 13 } },
                hAxis: { title: 'Clusters', titleTextStyle: { color: textColour, fontSize: 13 } },
                backgroundColor: { fill: 'transparent' }
            }
        };
    };

    // $scope.chart = $scope.doChart($scope.analysis.distributionForInstances);

});