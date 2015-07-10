package ikube.web.service;

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
import org.springframework.beans.factory.annotation.Qualifier;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    public static final String REQUEST = "request";
    private static final String SEPARATOR = "|";

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected IMonitorService monitorService;
    @Autowired
    protected ISearcherService searcherService;
    @Autowired
    @Qualifier(value = "ikube.cluster.IClusterManager")
    protected IClusterManager clusterManager;
    @Autowired
    protected IAnalyticsService analyticsService;

    /**
     * This method will build the response object, setting the headers for cross site JavaScript
     * operations, and for all the method types of the resource. The underlying Json converter will be
     * either Jackson or Gson, depending on the configuration.
     *
     * @param object the entity response object, the object that will be converted into Json for the client
     * @return the response object that will be used as the mechanism for transferring the entity to the client
     */
    protected Response buildResponse(final Object object) {
        Response.ResponseBuilder responseBuilder = Response
                .status(Response.Status.OK)//
                .header("Access-Control-Allow-Origin", "*") //
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        return responseBuilder.entity(object).build();
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

}