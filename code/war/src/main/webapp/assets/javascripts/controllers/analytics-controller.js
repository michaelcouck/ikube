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
    $scope.showAlgorithmOutput = false;
    $scope.showCorrelationCoefficients = false;
    $scope.showDistributionForInstances = false;

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
        algorithmOutput: undefined,
        correlation: true,
        distribution: true
    };

    $scope.context = {
        name: undefined, // 'weka',
        analyzer: undefined, // 'ikube.analytics.weka.WekaClassifier',
        filter: undefined, // 'weka.filters.unsupervised.attribute.StringToWordVector',
        algorithm: undefined, // 'weka.classifiers.functions.SMO',
        trainingData: undefined,
        maxTraining: 10000
    };

    $scope.doCreate = function () {
        var url = getServiceUrl('/ikube/service/analyzer/create');

        $scope.status = undefined;
        var promise = $http.post(url, $scope.context);
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
            var text = ['Trained analyzer : ', $scope.context.name, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/green_tick.png', 5);
        });
        promise.error(function (data, status) {
            $scope.status = status;
            var text = ['Failed to train analyzer : ', $scope.context.name, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/red_cross.png', 15);
        });
    };

    $scope.doBuild = function () {
        var url = getServiceUrl('/ikube/service/analyzer/build');
        var context = angular.copy($scope.context);
        context.name = $scope.analysis.analyzer;

        $scope.status = undefined;
        var promise = $http.post(url, context);
        promise.success(function (data, status) {
            $scope.status = status;
            var text = ['Built analyzer : ', $scope.context.name, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/green_tick.png', 5);
        });
        promise.error(function (data, status) {
            $scope.status = status;
            var text = ['Failed to build analyzer : ', $scope.context.name, ', status : ', $scope.status];
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
        $scope.analysis.correlationCoefficients = null;
        $scope.analysis.distributionForInstances = null;

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