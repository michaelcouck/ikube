package ikube.web.service;

import ikube.IConstants;
import ikube.cluster.listener.IListener;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.scheduling.schedule.Event;
import ikube.toolkit.ObjectToolkit;
import ikube.toolkit.SerializationUtilities;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

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
	public static final String SERVER = "/server";
	public static final String SERVERS = "/servers";
	public static final String INDEXES = "/indexes";
	public static final String INDEXING = "/indexing";
	public static final String SEARCHING = "/searching";
	public static final String ACTIONS = "/actions";
	public static final String START = "/start";
	public static final String TERMINATE = "/terminate";
	public static final String GET_PROPERTIES = "/get-properties";
	public static final String SET_PROPERTIES = "/set-properties";
	public static final String STARTUP_ALL = "/startup-all";
	public static final String TERMINATE_ALL = "/terminate-all";

	public static final String INDEX_CONTEXT = "/index-context";
	public static final String INDEX_CONTEXTS = "/index-contexts";

	public static final String DELETE_INDEX = "/delete-index";
	public static final String CPU_THROTTLING = "/cpu-throttling";
	public static final String ANALYZERS = "/analyzers";

	@GET
	@Path(Monitor.FIELDS)
	@Consumes(MediaType.APPLICATION_XML)
	public Response fields(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
		if (StringUtils.isEmpty(indexName)) {
			return buildJsonResponse(new String[0]);
		}
		return buildJsonResponse(monitorService.getIndexFieldNames(indexName));
	}

	@GET
	@Path(Monitor.INDEXES)
	@Consumes(MediaType.APPLICATION_XML)
	public Response indexes() {
		return buildJsonResponse(monitorService.getIndexNames());
	}

	@GET
	@SuppressWarnings("rawtypes")
	@Path(Monitor.INDEX_CONTEXT)
	@Consumes(MediaType.APPLICATION_XML)
	public Response indexContext(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
		IndexContext indexContext = cloneIndexContext(monitorService.getIndexContext(indexName));
		return buildJsonResponse(indexContext);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private IndexContext cloneIndexContext(final IndexContext indexContext) {
		IndexContext cloneIndexContext = (IndexContext) SerializationUtilities.clone(indexContext);
		cloneIndexContext.setChildren(null);
		// cloneIndexContext.setSnapshots(null);
		return cloneIndexContext;
	}

	@GET
	@SuppressWarnings({ "rawtypes" })
	@Path(Monitor.INDEX_CONTEXTS)
	@Consumes(MediaType.APPLICATION_XML)
	public Response indexContexts(@QueryParam(value = IConstants.SORT_FIELD) final String sortField,
			@QueryParam(value = IConstants.DESCENDING) final boolean descending) {
		List<IndexContext> indexContexts = new ArrayList<IndexContext>();
		for (final IndexContext indexContext : monitorService.getIndexContexts().values()) {
			IndexContext cloneIndexContext = cloneIndexContext(indexContext);
			indexContexts.add(cloneIndexContext);
		}
		// We sort on the parameter if not null, otherwise on the name field
		if (!StringUtils.isEmpty(sortField)) {
			Collections.sort(indexContexts, new Comparator<Object>() {
				@Override
				public int compare(final Object o1, final Object o2) {
					Object v1 = ObjectToolkit.getFieldValue(o1, sortField);
					Object v2 = ObjectToolkit.getFieldValue(o2, sortField);
					return descending ? CompareToBuilder.reflectionCompare(v1, v2) : -(CompareToBuilder.reflectionCompare(v1, v2));
				}
			});
		}
		return buildJsonResponse(indexContexts);
	}

	@GET
	@Path(Monitor.SERVER)
	@Consumes(MediaType.APPLICATION_XML)
	public Response server() {
		Server server = clusterManager.getServer();
		Server cloneServer = cloneServer(server);
		return buildJsonResponse(cloneServer);
	}

	@GET
	@Path(Monitor.SERVERS)
	@Consumes(MediaType.APPLICATION_XML)
	public Response servers() {
		List<Server> result = new ArrayList<Server>();
		Map<String, Server> servers = clusterManager.getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			Server cloneServer = cloneServer(server);
			result.add(cloneServer);
		}
		return buildJsonResponse(result);
	}

	private Server cloneServer(final Server server) {
		Server cloneServer = (Server) SerializationUtilities.clone(server);
		cloneServer.setIndexContexts(null);
		List<Action> actions = cloneServer.getActions();
		for (Action cloneAction : actions) {
			cloneAction.setServer(null);
		}
		return cloneServer;
	}

	@GET
	@Path(Monitor.INDEXING)
	@Consumes(MediaType.APPLICATION_XML)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response indexingStatistics() {
		Map<String, Server> servers = clusterManager.getServers();
		Object[] times = getTimes(servers, new ArrayList<Object>(Arrays.asList(addQuotes("Times"))));
		Object[][] data = new Object[servers.size() + 1][times.length];
		int serverIndex = 0;
		for (final Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			Object[] serverData = new Object[times.length];
			serverData[0] = addQuotes(server.getAddress());
			Arrays.fill(serverData, 1, serverData.length, new Long(0));
			List<IndexContext> indexContexts = server.getIndexContexts();
			for (final IndexContext indexContext : indexContexts) {
				List<Snapshot> snapshots = indexContext.getSnapshots();
				int index = 1;
				for (final Snapshot snapshot : snapshots) {
					if (serverData.length <= index) {
						break;
					}
					Long docsPerMinute = (Long) serverData[index];
					docsPerMinute += snapshot.getDocsPerMinute();
					serverData[index] = docsPerMinute;
					index++;
				}
			}
			data[serverIndex] = serverData;
			serverIndex++;
		}
		data[serverIndex] = times;
		String stringified = Arrays.deepToString(invertMatrix(data));
		return buildResponse().entity(stringified).build();
	}

	@GET
	@Path(Monitor.SEARCHING)
	@Consumes(MediaType.APPLICATION_XML)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response searchingStatistics() {
		Map<String, Server> servers = clusterManager.getServers();
		Object[] times = getTimes(servers, new ArrayList<Object>(Arrays.asList(addQuotes("Times"))));
		Object[][] data = new Object[servers.size() + 1][times.length];
		int serverIndex = 0;
		for (final Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			Object[] serverData = new Object[times.length];
			serverData[0] = addQuotes(server.getAddress());
			Arrays.fill(serverData, 1, serverData.length, new Long(0));
			List<IndexContext> indexContexts = server.getIndexContexts();
			for (final IndexContext indexContext : indexContexts) {
				List<Snapshot> snapshots = indexContext.getSnapshots();
				int index = 1;
				for (final Snapshot snapshot : snapshots) {
					if (serverData.length <= index) {
						break;
					}
					Long searchesPerMinute = (Long) serverData[index];
					searchesPerMinute += snapshot.getSearchesPerMinute();
					serverData[index] = searchesPerMinute;
					index++;
				}
			}
			data[serverIndex] = serverData;
			serverIndex++;
		}
		data[serverIndex] = times;
		String stringified = Arrays.deepToString(invertMatrix(data));
		return buildResponse().entity(stringified).build();
	}

	@GET
	@Path(Monitor.ACTIONS)
	@Consumes(MediaType.APPLICATION_XML)
	public Response actions() {
		List<Action> clonedActions = new ArrayList<Action>();
		Map<String, Server> servers = clusterManager.getServers();
		for (final Server server : servers.values()) {
			List<Action> actions = server.getActions();
			server.setActions(null);
			for (final Action action : actions) {
				Action clonedAction = (Action) SerializationUtilities.clone(action);
				server.setIndexContexts(null);
				clonedAction.setServer(server);
				clonedActions.add(clonedAction);
			}
		}
		return buildJsonResponse(clonedActions);
	}

	@GET
	@Path(Monitor.START)
	@Consumes(MediaType.APPLICATION_XML)
	public Response start(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
		monitorService.start(indexName);
		return buildResponse().build();
	}

	@GET
	@Path(Monitor.TERMINATE)
	@Consumes(MediaType.APPLICATION_XML)
	public Response terminate(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
		monitorService.terminate(indexName);
		return buildResponse().build();
	}

	@GET
	@Path(Monitor.GET_PROPERTIES)
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProperties() {
		return buildJsonResponse(monitorService.getProperties());
	}

	@POST
	@SuppressWarnings("unchecked")
	@Path(Monitor.SET_PROPERTIES)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setProperties(@Context final HttpServletRequest request, @Context final UriInfo uriInfo) throws IOException {
		Map<String, String> filesAndProperties = unmarshall(Map.class, request);
		monitorService.setProperties(filesAndProperties);
		return buildResponse().build();
	}

	@GET
	@Path(Monitor.TERMINATE_ALL)
	@Consumes(MediaType.APPLICATION_XML)
	public Response terminateAll() {
		monitorService.terminateAll();
		return buildResponse().build();
	}

	@GET
	@Path(Monitor.STARTUP_ALL)
	@Consumes(MediaType.APPLICATION_XML)
	public Response startupAll() {
		monitorService.startupAll();
		return buildResponse().build();
	}

	@GET
	@Path(Monitor.DELETE_INDEX)
	@Consumes(MediaType.APPLICATION_XML)
	public Response delete(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
		long time = System.currentTimeMillis();
		Event startEvent = IListener.EventGenerator.getEvent(Event.DELETE_INDEX, time, indexName, Boolean.FALSE);
		logger.info("Sending delete event : " + ToStringBuilder.reflectionToString(startEvent));
		clusterManager.sendMessage(startEvent);
		return buildResponse().build();
	}

	@GET
	@Path(Monitor.CPU_THROTTLING)
	@Consumes(MediaType.APPLICATION_XML)
	public Response cpuThrottling() {
		monitorService.cpuThrottling();
		return buildResponse().build();
	}

	@GET
	@Path(Monitor.ANALYZERS)
	@Consumes(MediaType.APPLICATION_XML)
	public Response analyzers() {
		String[] analyzers = analyticsService.getAnalyzers().keySet().toArray(new String[analyticsService.getAnalyzers().keySet().size()]);
		return buildJsonResponse(analyzers);
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	private IndexContext getIndexContextFromServer(final String indexName, final Server server) {
		List<IndexContext> indexContexts = server.getIndexContexts();
		if (indexContexts != null) {
			for (final IndexContext indexContext : indexContexts) {
				if (indexContext.getIndexName().equals(indexName)) {
					return indexContext;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unused")
	private void addFieldValues(final Action action, final Map<String, Object> actionData) {
		ReflectionUtils.doWithFields(action.getClass(), new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {
				if (Modifier.isStatic(field.getModifiers())) {
					return;
				}
				field.setAccessible(Boolean.TRUE);
				Object value = ReflectionUtils.getField(field, action);
				if (value != null) {
					value = value.toString();
				}
				actionData.put(field.getName(), value);
			}
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object[] getTimes(final Map<String, Server> servers, final ArrayList<Object> times) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		outer: for (final Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			List<IndexContext> indexContexts = server.getIndexContexts();
			for (final IndexContext indexContext : indexContexts) {
				List<Snapshot> snapshots = indexContext.getSnapshots();
				for (final Snapshot snapshot : snapshots) {
					gregorianCalendar.setTime(snapshot.getTimestamp());
					int hour = gregorianCalendar.get(Calendar.HOUR_OF_DAY);
					int minute = gregorianCalendar.get(Calendar.MINUTE);
					times.add(addQuotes(getDoubleTime(hour, minute).toString()));
				}
				break outer;
			}
		}
		return times.toArray(new Object[times.size()]);
	}

	private String addQuotes(final String string) {
		return "\"" + string + "\"";
	}

	private Double getDoubleTime(final int hour, final int minute) {
		return Double.parseDouble(hour + "." + minute);
	}

}