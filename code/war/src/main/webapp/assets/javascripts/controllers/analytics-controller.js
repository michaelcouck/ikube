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

    $scope.contexts = undefined;

    $scope.analysis = {
        context: undefined,
        clazz: undefined,
        input: undefined,
        output: undefined,
        addAlgorithmOutput: true
    };

    $scope.analyzers = [
        'ikube.analytics.weka.WekaClassifier',
        'ikube.analytics.neuroph.NeurophAnalyzer',
        'ikube.analytics.weka.WekaClusterer'
    ];

    $scope.filters = [
        'weka.filters.unsupervised.attribute.StringToNominal',
        'weka.filters.unsupervised.attribute.StringToWordVector',
        'weka.filters.unsupervised.attribute.ReplaceMissingValues',
        'weka.filters.unsupervised.attribute.ClassAssigner',
        'weka.filters.unsupervised.attribute.RandomSubset',
        'weka.filters.unsupervised.attribute.RemoveUseless'
    ];

    $scope.algorithms = [
        'weka.classifiers.lazy.IB1',
        'weka.classifiers.bayes.ComplementNaiveBayes',
        'weka.classifiers.meta.ClassificationViaClustering',
        'weka.classifiers.functions.LeastMedSq',
        'weka.classifiers.functions.SMOreg',
        'weka.classifiers.functions.SimpleLogistic',
        'weka.classifiers.functions.Logistic',
        'weka.classifiers.functions.SMO',
        'weka.classifiers.meta.AdditiveRegression',
        'weka.classifiers.functions.LinearRegression',
        'weka.classifiers.functions.IsotonicRegression',
        'weka.classifiers.meta.RegressionByDiscretization'
    ];

    $scope.context = {
        name: undefined,

        analyzer: undefined,
        algorithms: undefined,
        filters: undefined,

        options: undefined, // -N 6 (six clusters for example)
        trainingDatas: undefined,
        maxTrainings: undefined,
        fileNames: undefined
    };

    // Listen for the file selected event
    $scope.$on("fileSelected", function (event, args) {
        $scope.$apply(function () {
            $scope.fileNames = args.file;
        });
    });

    $scope.doCreate = function () {
        var url = getServiceUrl('/ikube/service/analyzer/create');

        //noinspection JSUnresolvedVariable
        var context = angular.copy($scope.context);
        if (!!context.algorithms) {
            context.algorithms = context.algorithms.split(',');
        }
        if (!!context.filters) {
            context.filters = context.filters.split(',');
        }
        if (!!context.trainingDatas) {
            context.trainingDatas = context.trainingDatas.split(',');
        }
        if (!!context.maxTrainings) {
            context.maxTrainings = context.maxTrainings.split(',');
        }
        if (!!context.options) {
            context.options = context.options.split(',');
        }
        if (!!context.fileNames) {
            context.fileNames = context.fileNames.split(',');
        }

        $scope.status = undefined;

        var promise = $http.post(url, context);

        promise.success(function (data, status) {
            $scope.status = status;
            $scope.doContexts();
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

        $scope.analysis.clazz = undefined;
        $scope.analysis.output = undefined;
        $scope.analysis.algorithmOutput = undefined;

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

    $scope.doContext = function () {
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