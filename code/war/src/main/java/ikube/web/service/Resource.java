package ikube.web.service;

import ikube.IConstants;
import ikube.action.index.parse.HtmlParser;
import ikube.analytics.IAnalyticsService;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.search.ISearcherService;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This is the base class for all web services, common logic and properties.
 *
 * @author Michael couck
 * @version 01.00
 * @since 20-11-2012
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public abstract class Resource {

    /**
     * Constants for the paths to the web services.
     */
    public static final String REQUEST = "request";
    private static final String SEPARATOR = "|";

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected IMonitorService monitorService;
    @Autowired
    protected ISearcherService searcherService;
    @Autowired
    protected IClusterManager clusterManager;
    @Autowired
    protected IAnalyticsService analyticsService;

    /**
     * This method will create the response builder, then convert the results to Json and add them the
     * response payload, then build the response object from the builder.
     *
     * @param result the data to convert to Json
     * @return the Json response object to send to the caller/client
     */
    protected Response buildJsonResponse(final Object result) {
        if (result == null) {
            return buildResponse().build();
        }
        String jsonString = IConstants.GSON.toJson(result);
        if (logger.isDebugEnabled()) {
            logger.debug("Response size : " + jsonString.length());
        }
        return buildResponse().entity(jsonString).build();
    }

    /**
     * This method will create the response builder, then convert the results to xml and add them
     * the response payload, then build the response object from the builder.
     *
     * @param result the data to convert to xml
     * @return the xml response object to send to the caller/client
     */
    protected Response buildXmlResponse(final Object result) {
        if (result == null) {
            return buildResponse().build();
        }
        return buildResponse().entity(SerializationUtilities.serialize(result)).build();
    }

    /**
     * This method will just create the response builder and add some headers for cross site scripting.
     *
     * @return the response builder for the category
     */
    protected ResponseBuilder buildResponse() {
        return Response.status(Response.Status.OK)//
                .header("Access-Control-Allow-Origin", "*") //
                .header("Access-Control-Allow-Methods", "GET, POST, PUT");
    }

    <T> T unmarshall(final Class<T> clazz, final HttpServletRequest request) {
        try {
            String json = FileUtilities.getContents(request.getInputStream(), Integer.MAX_VALUE).toString();
            T t = IConstants.GSON.fromJson(json, clazz);
            if (t == null) {
                t = newInstance(clazz);
            }
            return t;
        } catch (IOException e) {
            return newInstance(clazz);
        }
    }

    private <T> T newInstance(final Class<T> clazz) {
        try {
            // If we don't have the class in the input stream then create one for the caller
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Couldn't unmarshall to : " + clazz, e);
        }
    }

    String[] split(final String string) {
        if (StringUtils.isEmpty(string)) {
            return new String[0];
        }
        String cleaned = string;
        HtmlParser htmlParser = new HtmlParser();
        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(string.getBytes());
            htmlParser.parse(inputStream, outputStream);
            cleaned = outputStream.toString();
        } catch (Exception e) {
            logger.error("Exception cleaning the search string of html : " + string, e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        return StringUtils.split(cleaned, SEPARATOR);
    }

    Object[][] invertMatrix(Object[][] matrix) {
        final int m = matrix.length;
        final int n = matrix[0].length;
        Object[][] inverted = new Object[n][m];
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                inverted[c][m - 1 - r] = matrix[r][c];
            }
        }
        return inverted;
    }

}