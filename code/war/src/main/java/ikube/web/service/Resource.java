package ikube.web.service;

import ikube.IConstants;
import ikube.action.index.parse.HtmlParser;
import ikube.analytics.IAnalyticsService;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.search.ISearcherService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import static ikube.toolkit.FileUtilities.getContents;

/**
 * This is the base class for all web services, common logic and properties.
 *
 * @author Michael couck
 * @version 01.00
 * @since 20-11-2012
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public abstract class Resource {

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
     * TODO: Document me...
     *
     * @return the bla...
     */
    protected Response buildResponse(final Object object) {
        Object entity = object;
        if (Collection.class.isAssignableFrom(entity.getClass())) {
            // entity = entity.toString();
            try {
                entity = IConstants.GSON.toJson(entity);
            } catch (final Throwable e) {
                logger.error("Exception converting to Json : " + object, e);
            }
        }
        return Response.status(Response.Status.OK)//
                .header("Access-Control-Allow-Origin", "*") //
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                .entity(entity).build();
    }

    <T> T unmarshall(final Class<T> clazz, final HttpServletRequest request) {
        try {
            String json = getContents(request.getInputStream(), Integer.MAX_VALUE).toString();
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