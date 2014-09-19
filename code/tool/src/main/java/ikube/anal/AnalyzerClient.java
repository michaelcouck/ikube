package ikube.anal;

import ikube.Client;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Context;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

import static ikube.IConstants.DELIMITER_CHARACTERS;
import static ikube.toolkit.FileUtilities.getContent;
import static ikube.toolkit.HttpClientUtilities.doPost;
import static org.springframework.util.ReflectionUtils.MethodCallback;
import static org.springframework.util.ReflectionUtils.doWithMethods;

/**
 * TODO: Document this client if it becomes used.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 16-09-2014
 */
@SuppressWarnings("UnusedDeclaration")
public class AnalyzerClient extends Client {

    public static void main(final String[] args) throws CmdLineException {
        new AnalyzerClient().doMain(args);
    }

    @Option(name = "-p", usage = "The port of the server")
    private int port;
    @Option(name = "-h", usage = "The host of the server, localhost, or ikube.be for example")
    private String host;
    @Option(name = "-n", usage = "The name of the context or analyzer hierarchy of objects")
    private String name;
    @Option(name = "-f", usage = "The name of the file, on the local file system, to load with the training data for the analyzer, absolute path")
    private String inputFilePath;
    @Option(name = "-op", usage = "Operations, functions or methods to call on the analyzer api, could be create, build, train, analyze or destroy")
    private String operationNames;
    @Option(name = "-an", usage = "The name of the analyzer, in the case of clustering it is WekaClusterer, or for a neural network it is NeurophAnalyzer")
    private String analyzerName;
    @Option(name = "-al", usage = "The name of the underlying algorithm, for example SMO for support vectors and MultiLayerPerceptron for a neural network")
    private String algorithmName;
    @Option(name = "-fi", usage = "Filters that convert the data into another format, like string to vector for example")
    private String filterName;
    @Option(name = "-m", usage = "The maximum number of instances to accept as training, iterations in the case of neural networks")
    private int maxTraining;
    @Option(name = "-o", usage = "Any options that the algorithm supports, like R or number of clusters, folds etc.")
    private String options;

    @Option(name = "-i", usage = "In the case of an analysis, this is the input to be analyzed, could be a string or a vector of integers")
    private String input;

    @Option(name = "-ao", usage = "Whether to include the output from the algorithm, this can be very expensive")
    private boolean addAlgorithmOutput = Boolean.FALSE;

    private void doMain(final String[] args) throws CmdLineException {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(140);
        parser.parseArgument(args);

        final AnalyzerClient analClient = this;

        doWithMethods(this.getClass(), new MethodCallback() {
            @Override
            public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
                for (final String operationName : StringUtils.split(operationNames, DELIMITER_CHARACTERS)) {
                    if (operationName.equals(method.getName())) {
                        try {
                            method.invoke(analClient);
                        } catch (final InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
    }

    void create() throws MalformedURLException {
        File inputFile = new File(inputFilePath);
        String trainingData = getContent(inputFile);
        Context context = new Context();
        context.setName(name);
        context.setAnalyzer(analyzerName);
        context.setAlgorithms(algorithmName);
        if (StringUtils.isNotEmpty(filterName)) {
            context.setFilters(filterName);
        }
        if (maxTraining > 0) {
            context.setMaxTrainings(maxTraining);
        }
        if (StringUtils.isNotEmpty(options)) {
            context.setOptions(IConstants.GSON.fromJson(options, String[].class));
        }
        if (StringUtils.isNotEmpty(trainingData)) {
            context.setTrainingDatas(trainingData);
        }
        String url = getUrl(host, port, "analyzer", "create");
        context = doPost(url, context, Context.class);
        logger.error("Context : " + ToStringBuilder.reflectionToString(context));
    }

    void train() {
    }

    void build() throws MalformedURLException {
        Analysis analysis = new Analysis();
        analysis.setContext(name);

        String url = getUrl(host, port, "analyzer", "build");
        Context context = doPost(url, analysis, Context.class);
    }

    @SuppressWarnings("unchecked")
    void analyze() throws MalformedURLException {
        Analysis analysis = new Analysis();
        analysis.setContext(name);
        analysis.setInput(input);
        analysis.setAddAlgorithmOutput(Boolean.TRUE);

        String url = getUrl(host, port, "analyzer", "analyze");
        Analysis result = doPost(url, analysis, Analysis.class);
        logger.error("Analysis : " + ToStringBuilder.reflectionToString(result));
    }

    void destroy() throws MalformedURLException {
        Analysis analysis = new Analysis();
        analysis.setContext(name);

        String url = getUrl(host, port, "analyzer", "destroy");
        Context context = doPost(url, analysis, Context.class);
    }

}