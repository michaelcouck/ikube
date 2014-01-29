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

    $scope.analysis = {
        clazz: undefined,
        input: undefined,
        correlation: false,
        distribution: false
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
        var analysis = angular.copy($scope.analysis);

        analysis.clazz = null;
        analysis.output = null;
        analysis.exception = null;
        analysis.algorithmOutput = null;
        analysis.correlationCoefficients = null;
        analysis.distributionForInstances = null;

        var promise = $http.post(url, analysis);
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
        promise.success(function (data, status) {
            $scope.status = status;
            $scope.analyzers = data;
        });
        promise.error(function (data, status) {
            $scope.status = status;
        });
    };

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

    $scope.doAnalyzers();
    // $scope.chart = $scope.doChart($scope.analysis.distributionForInstances);

});