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

    $scope.analyzers = undefined;
    $scope.algorithms = undefined;
    $scope.filters = undefined;

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
        $scope.splitProperty(context, 'algorithms', context.algorithms);
        $scope.splitProperty(context, 'filters', context.filters);
        $scope.splitProperty(context, 'trainingDatas', context.trainingDatas);
        $scope.splitProperty(context, 'maxTrainings', context.maxTrainings);
        $scope.splitProperty(context, 'options', context.options);
        $scope.splitProperty(context, 'fileNames', context.fileNames);
    };
    $scope.splitProperty = function(context, name, property) {
        if (!!property) {
            context[name] = property.split(',');
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

    $scope.getSubTypes = function(type, pakkage, listener) {
        var url = getServiceUrl('/ikube/service/monitor/sub-types');
        var parameters = {type : type, package : pakkage};
        var config = { params : parameters };
        $scope.status = undefined;
        var promise = $http.get(url, config);
        promise.success(function (data, status) {
            $scope.status = status;
            return $timeout(function() {
                $scope.$emit(listener, {data : data});
            }, 250);

        });
        promise.error(function (data, status) {
            $scope.status = status;
            var text = ['Couldn\'t get filters for type : ', type, ', status : ', $scope.status];
            notificationService.notify(text, '/ikube/assets/images/icons/red_cross.png', 15);
        });
    };

    var analyzerType = 'ikube.analytics.IAnalyzer';
    var analyzersListener = 'analyzers-listener';
    var algorithmsListener = 'algorithms-listener';
    var filtersListener = 'filters-listener';
    $scope.$on(analyzersListener, function (event, args) {
        $scope.$apply(function () {
            $scope.analyzers = args.data;
        });
    });
    $scope.$on(algorithmsListener, function (event, args) {
        $scope.$apply(function () {
            $scope.algorithms = args.data;
        });
    });
    $scope.$on(filtersListener, function (event, args) {
        $scope.$apply(function () {
            $scope.filters = args.data;
        });
    });
    $scope.getAnalyzers = function() {
        $scope.getSubTypes(analyzerType, 'ikube.analytics', analyzersListener);
    };
    $scope.getAlgorithms = function() {
        return $timeout(function() {
            if (!$scope.context.analyzer) {
                return;
            }
            var analyzer = $scope.context.analyzer.toString();
            var algorithmType = undefined;
            var algorithmPakkage = undefined;

            if (analyzer.search('ikube.analytics.weka.WekaClassifier') > -1) {
                algorithmType = 'weka.classifiers.Classifier';
                algorithmPakkage = 'weka.classifiers';
            } else if (analyzer.search('ikube.analytics.weka.WekaClusterer') > -1) {
                algorithmType = 'weka.clusterers.Clusterer';
                algorithmPakkage = 'weka.clusterer';
            } else if (analyzer.search('ikube.analytics.neuroph.NeurophAnalyzer') > -1) {
                algorithmType = 'org.neuroph.core.NeuralNetwork';
                algorithmPakkage = 'org.neuroph.nnet';
                $scope.filters = undefined;
            } else if (analyzer.search('ikube.analytics.weka.WekaForecastClassifier') > -1) {
                $scope.algorithms = undefined;
                $scope.filters = undefined;
                return;
            } else {
                throw 'Could not find analyzer type';
            }
            $scope.getSubTypes(algorithmType, algorithmPakkage, algorithmsListener);
        }, 250);
    };
    $scope.getFilters = function() {
        var algorithm = $scope.context.algorithms.toString();
        if (algorithm.search('weka') > -1) {
            $scope.getSubTypes('weka.filters.Filter', 'weka.filters', filtersListener);
        }
    };

    $scope.getContexts();
    $scope.getAnalyzers();

});