package ikube.web.service;

import ikube.IConstants;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Michael couck
 * @since 16.10.12
 * @version 01.00
 */
@Component
@Path(Monitor.MONITOR)
@Scope(Monitor.REQUEST)
@Produces(MediaType.TEXT_PLAIN)
public class Monitor extends Resource {

	/** Constants for the paths to the web services. */
	public static final String SERVICE = "/service";
	public static final String MONITOR = "/monitor";

	public static final String FIELDS = "/fields";
	public static final String INDEXES = "/indexes";
	public static final String GEOSPATIAL = "/geospatial";

	@GET
	@Path(Monitor.FIELDS)
	@Consumes(MediaType.APPLICATION_XML)
	public Response fields(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
		return buildResponse(monitorService.getIndexFieldNames(indexName));
	}

	@GET
	@Path(Monitor.INDEXES)
	@Consumes(MediaType.APPLICATION_XML)
	public Response indexes() {
		return buildResponse(monitorService.getIndexNames());
	}

	@GET
	@Path(Monitor.GEOSPATIAL)
	@Consumes(MediaType.APPLICATION_XML)
	public Response geospatial(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
		return buildResponse(monitorService.getIndexContext(indexName));
	}

}