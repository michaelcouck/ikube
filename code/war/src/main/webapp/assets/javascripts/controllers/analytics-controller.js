/**
 * @author Michael Couck
 * @since 24-11-2013
 *
 * @param $scope
 * @param $http
 * @param $injector
 * @param $timeout
 * @param $location
 */
module.controller('AnalyticsController', function ($scope, $http, $injector, $timeout, $location, notificationService) {

    $scope.someData = [[1,2,3], [1,2,3], [1,2,3]];

    $scope.status = undefined;
    $scope.contexts = undefined;
    $scope.file = undefined;
    $scope.fileUploadStatus = undefined;
    $scope.matrices = undefined;
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
            $scope.file = args.file;
            $scope.context.fileNames = args.file.name;
        });
    });

    // Listen for the file to be uploaded
    $scope.$on("fileUploaded", function (event, args) {
        $scope.fileUploadStatus = args.status;
    });

    $scope.doCreate = function () {

        if ($scope.fileUploadStatus != 200) {
            var text = ['Please upload a data file before creating the analyzer : ', $scope.fileUploadStatus];
            notificationService.notify(text, '/ikube/assets/images/icons/red_cross.png', 15);
            return;
        }

        var url = getServiceUrl('/ikube/service/analyzer/create');

        var context = angular.copy($scope.context);
        $scope.splitProperties(context);

        $scope.status = undefined;

        var promise = $http.post(url, context);

        promise.success(function (data, status) {
            $scope.status = status;
            var text = ['Created analyzer : ', $scope.context.name, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/green_tick.png', 5);
            // Doesn't work
            // $location.path(configureUrl);
            document.location.href = '/ikube/analytics/configure.html';
        });
        promise.error(function (data, status) {
            $scope.status = status;
            var text = ['Exception creating analyzer : ', $scope.context.name, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/red_cross.png', 15);
            $scope.getContexts();
        });
    };

    $scope.splitProperties = function(context) {
        // $scope.fileNames = $scope.file;
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

        $scope.analysis.clazz = undefined;
        $scope.analysis.output = undefined;
        $scope.analysis.algorithmOutput = undefined;

        $scope.status = undefined;
        var promise = $http.post(url, $scope.analysis);
        promise.success(function (data, status) {
            $scope.status = status;
            $scope.analysis.clazz = data.clazz;
            $scope.analysis.output = data.output;
        });
        promise.error(function (data, status) {
            $scope.status = status;
        });
    };

    $scope.getContexts = function () {
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

    $scope.getContext = function () {
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

    $scope.getData = function() {
        var url = getServiceUrl('/ikube/service/analyzer/data');
        return $timeout(function() {
            $scope.status = undefined;
            var promise = $http.post(url, $scope.context);
            promise.success(function (data, status) {
                $scope.status = status;
                $scope.matrices = data;
            });
            promise.error(function (data, status) {
                $scope.status = status;
            });
        }, 1000);
    };

    $scope.getContexts();

});