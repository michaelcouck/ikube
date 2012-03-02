package ikube.web.service;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Search;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * @author Michael couck
 * @since 01.03.12
 * @version 01.00
 */
@Component
@Path(Autocomplete.AUTOCOMPLETE)
@Scope(Autocomplete.REQUEST)
@Produces(MediaType.TEXT_PLAIN)
public class Autocomplete {

	private static final Logger LOGGER = LoggerFactory.getLogger(Autocomplete.class);

	/** Constants for the paths to the web services. */
	public static final String SINGLE = "/single";
	public static final String REQUEST = "request";
	public static final String AUTOCOMPLETE = "/autocomplete";

	@Autowired
	private IDataBase dataBase;
	private Object[] values = new Object[1];
	private String[] names = { IConstants.SEARCH_STRINGS };
	private Gson gson = new Gson();

	@GET
	@Path(Autocomplete.SINGLE)
	@Consumes(MediaType.APPLICATION_XML)
	public String autocomplete(@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings) {
		values[0] = searchStrings + "%";
		List<Search> searches = dataBase.find(Search.class, Search.SELECT_FROM_SEARCH_BY_SEARCH_STRINGS_LIKE, names, values, 0, 10);
		LOGGER.info("Searches : " + searches);
		return gson.toJson(searches);
	}

}