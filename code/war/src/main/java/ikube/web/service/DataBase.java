package ikube.web.service;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.toolkit.ObjectToolkit;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Michael couck
 * @since 10.09.13
 * @version 01.00
 */
@Component
@Path(DataBase.DATABASE)
@Scope(DataBase.REQUEST)
@Produces(MediaType.TEXT_PLAIN)
public class DataBase extends Resource {

	/** Constants for the paths to the web services. */
	public static final String DATABASE = "/database";
	public static final String REQUEST = "request";

	public static final String ENTITY = "/entity";
	public static final String ENTITIES = "/entities";
	public static final String ENTITY_CREATE = "/entity/create";
	public static String[] EXCLUDED_PROPERTIES = { "id", "parent", "children", "snapshot", "snapshots", "availableDiskSpace", "exceptions", "indexing",
			"numDocsForSearchers", "open" };

	@Autowired
	private IDataBase dataBase;

	@GET
	@Path(DataBase.ENTITY)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public String entity(@QueryParam(value = IConstants.CLASS)
	final String clazz, @QueryParam(value = IConstants.ID)
	final long id) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Object entity = dataBase.find(Class.forName(clazz), id);
		if (entity == null) {
			entity = Class.forName(clazz).newInstance();
			ObjectToolkit.populateFields(entity, Boolean.TRUE, 1, EXCLUDED_PROPERTIES);
		}
		return gson.toJson(entity);
	}

	@GET
	@Path(DataBase.ENTITIES)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public String entities(@QueryParam(value = IConstants.CLASS)
	final String clazz, @QueryParam(value = IConstants.START_INDEX)
	final int startIndex, @QueryParam(value = IConstants.END_INDEX)
	final int endIndex) throws ClassNotFoundException {
		List<?> list = dataBase.find(Class.forName(clazz), startIndex, endIndex);
		return gson.toJson(list);
	}

	@POST
	@Path(DataBase.ENTITY_CREATE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String createIndexContext(final String entity) throws ClassNotFoundException {
		logger.info("Entity : " + entity);
		return gson.toJson(createEntity(IndexContext.class, entity));
	}

	private <T> T createEntity(final Class<T> type, final String entity) {
		T t = gson.fromJson(entity, type);
		logger.info("Entity : " + t);
		dataBase.persist(t);
		return t;
	}

}