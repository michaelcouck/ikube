package ikube.web.service;

import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.search.ISearcherService;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * This is the base class for all web services, sommon logic and properties.
 * 
 * @author Michael couck
 * @since 20.11.12
 * @version 01.00
 */
public abstract class Resource {

	/** Constants for the paths to the web services. */
	public static final String REQUEST = "request";

	public static final String SINGLE = "/single";
	public static final String MULTI = "/multi";
	public static final String MULTI_SORTED = "/multi/sorted";
	public static final String MULTI_ALL = "/multi/all";
	public static final String MULTI_SPATIAL = "/multi/spatial";
	public static final String MULTI_SPATIAL_ALL = "/multi/spatial/all";
	public static final String MULTI_ADVANCED = "/multi/advanced";
	public static final String MULTI_ADVANCED_ALL = "/multi/advanced/all";
	public static final String NUMERIC_ALL = "/numeric/all";
	public static final String NUMERIC_RANGE = "/numeric/range";
	public static final String COMPLEX = "/complex";

	public static final String SEPARATOR = ",;:|";

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected Gson gson;
	@Autowired
	protected IMonitorService monitorService;
	@Autowired
	protected ISearcherService searcherService;
	@Autowired
	protected IClusterManager clusterManager;

	{
		gson = new Gson();
	}

	/**
	 * This method will create the response builder, then convert the results to Json and add them the the response payload, then build the
	 * response object from the builder.
	 * 
	 * @param result the result to convert to the Json response
	 * @return the Json response object to send to the caller/client
	 */
	protected Response buildResponse(final Object result) {
		String jsonString = gson.toJson(result);
		return buildResponse().entity(jsonString).build();
	}

	/**
	 * This method will just create the response builder and add some headers for cross site scripting.
	 * 
	 * @return the response builder for the result
	 */
	protected ResponseBuilder buildResponse() {
		return Response.status(200).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
	}

}
