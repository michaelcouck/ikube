package ikube.anal;

import ikube.Client;
import ikube.model.Analysis;
import ikube.model.Context;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

import static ikube.IConstants.DELIMITER_CHARACTERS;
import static ikube.toolkit.FileUtilities.findFileRecursively;
import static ikube.toolkit.FileUtilities.getContent;
import static ikube.toolkit.HttpClientUtilities.doPost;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 16-09-2014
 */
@SuppressWarnings("UnusedDeclaration")
public class AnalyzerClient extends Client {

    @Option(name = "-p", usage = "")
    private int port;
    @Option(name = "-h", usage = "")
    private String host;

    @Option(name = "-n", usage = "The name of the context or analyzer hierarchy of objects")
    private String name;
    @Option(name = "-f", usage = "")
    private String fileName;
    @Option(name = "-c", usage = "")
    private String contextName;
    @Option(name = "-op", usage = "")
    private String operationNames;
    @Option(name = "-an", usage = "")
    private String analyzerName;
    @Option(name = "-al", usage = "")
    private String algorithmName;
    @Option(name = "-fi", usage = "")
    private String filterName;
    @Option(name = "-m", usage = "")
    private int maxTraining;
    @Option(name = "-o", usage = "")
    private String options;

    @Option(name = "-i", usage = "")
    private String input;

    public static void main(final String[] args) throws CmdLineException {
        new AnalyzerClient().doMain(args);
    }

    private void doMain(final String[] args) throws CmdLineException {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(140);
        parser.parseArgument(args);

        final AnalyzerClient analClient = this;

        ReflectionUtils.doWithMethods(this.getClass(), new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
                for (final String operationName : StringUtils.split(operationNames, DELIMITER_CHARACTERS)) {
                    if (operationName.equals(method.getName())) {
                        try {
                            method.invoke(analClient);
                        } catch (final InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void create() throws MalformedURLException {
        File inputDataFile = findFileRecursively(new File("."), fileName);
        String trainingData = getContent(inputDataFile);
        Context context = new Context();
        context.setName(name);
        context.setAnalyzer(analyzerName);
        context.setAlgorithms(algorithmName);
        context.setFilters(filterName);
        context.setMaxTrainings(maxTraining);
        context.setOptions(options);
        context.setTrainingDatas(trainingData);

        String url = getUrl(host, port, "analyzer", "create");
        context = doPost(url, context, Context.class);
    }

    private void train() {
    }

    private void build() throws MalformedURLException {
        Analysis analysis = new Analysis();
        analysis.setContext(contextName);

        String url = getUrl(host, port, "analyzer", "build");
        Context context = doPost(url, analysis, Context.class);
    }

    @SuppressWarnings("unchecked")
    private void analyze() throws MalformedURLException {
        Analysis analysis = new Analysis();
        analysis.setContext(contextName);
        analysis.setInput(input);

        String url = getUrl(host, port, "analyzer", "analyze");
        Analysis result = doPost(url, analysis, Analysis.class);
    }

    private void destroy() throws MalformedURLException {
        Analysis analysis = new Analysis();
        analysis.setContext(contextName);

        String url = getUrl(host, port, "analyzer", "destroy");
        Context context = doPost(url, analysis, Context.class);
    }

}