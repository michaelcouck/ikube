package ikube.web.service;

import ikube.IConstants;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * TODO Implement this and the architecture of course...
 * 
 * @author Michael couck
 * @since 02.07.13
 * @version 01.00
 */
@Component
@Path(Classifier.CLASSIFIER)
@Scope(Resource.REQUEST)
@Produces(MediaType.TEXT_PLAIN)
public class Classifier extends Resource {

	public static final String CLASSIFY = "/classify";
	public static final String CLASSIFIER = "/classifier/json";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(Classifier.class);

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	@GET
	@Path(Classifier.CLASSIFY)
	@Consumes(MediaType.APPLICATION_XML)
	public Response classify(@QueryParam(value = IConstants.CONTENT) final String content) throws IOException {
		return buildResponse("nothing");
	}

}