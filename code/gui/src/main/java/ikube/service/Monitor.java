package ikube.service;

import ikube.IConstants;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
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
public class Monitor {

	/** Constants for the paths to the web services. */
	public static final String REQUEST = "request";
	public static final String SERVICE = "/service";
	public static final String MONITOR = "/monitor";

	public static final String FIELDS = "/fields";
	public static final String INDEXES = "/indexes";
	public static final String GEOSPATIAL = "/geospatial";

	@Autowired
	private IMonitorService monitorService;

	@GET
	@Path(Monitor.FIELDS)
	@Consumes(MediaType.APPLICATION_XML)
	public String fields(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
		return toSeparatedString(';', monitorService.getIndexFieldNames(indexName));
	}

	@GET
	@Path(Monitor.INDEXES)
	@Consumes(MediaType.APPLICATION_XML)
	public String indexes() {
		return toSeparatedString(';', monitorService.getIndexNames());
	}

	private <T> String toSeparatedString(char separator, final T... objects) {
		if (objects == null || objects.length == 0) {
			return null;
		}
		StringBuilder stringBuilder = new StringBuilder();
		boolean first = Boolean.TRUE;
		for (final Object object : objects) {
			if (!first) {
				stringBuilder.append(separator);
			}
			first = Boolean.FALSE;
			stringBuilder.append(object);
		}
		return stringBuilder.toString();
	}

	@GET
	@Path(Monitor.GEOSPATIAL)
	@Consumes(MediaType.APPLICATION_XML)
	public String geospatial(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
		return Boolean.toString(monitorService.getIndexContext(indexName).isAddress());
	}

}