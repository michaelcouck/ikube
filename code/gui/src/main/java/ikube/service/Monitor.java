package ikube.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

	public static final String INDEXES = "/indexes";

	@Autowired
	private IMonitorService monitorService;

	@GET
	@Path(Monitor.INDEXES)
	@Consumes(MediaType.APPLICATION_XML)
	public String[] indexes() {
		return monitorService.getIndexNames();
	}

}