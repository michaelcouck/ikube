package ikube.web.service;

import ikube.IConstants;
import ikube.listener.Event;
import ikube.listener.ListenerManager;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.toolkit.SerializationUtilities;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
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
	@SuppressWarnings("rawtypes")
	@Path(Monitor.INDEX_CONTEXT)
	@Consumes(MediaType.APPLICATION_XML)
	public Response indexContext(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
		IndexContext indexContext = getIndexContext(indexName);
		return buildResponse(indexContext);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private IndexContext getIndexContext(final String indexName) {
		IndexContext indexContext = monitorService.getIndexContext(indexName);
		indexContext.isOpen();
		indexContext.isIndexing();
		indexContext.getNumDocs();
		indexContext.getIndexSize();
		indexContext.getLatestIndexTimestamp();
		IndexContext cloneIndexContext = (IndexContext) SerializationUtilities.clone(indexContext);
		cloneIndexContext.setChildren(null);
		cloneIndexContext.setIndexables(null);
		cloneIndexContext.setSnapshots(null);
		return cloneIndexContext;
	}

	@GET
	@SuppressWarnings({ "rawtypes" })
	@Path(Monitor.INDEX_CONTEXTS)
	@Consumes(MediaType.APPLICATION_XML)
	public Response indexContexts() {
		List<IndexContext> indexContexts = new ArrayList<IndexContext>();
		for (final IndexContext indexContext : monitorService.getIndexContexts().values()) {
			IndexContext cloneIndexContext = getIndexContext(indexContext.getIndexName());
			indexContexts.add(cloneIndexContext);
		}
		return buildResponse(indexContexts);
	}

	@GET
	@Path(Monitor.SERVERS)
	@Consumes(MediaType.APPLICATION_XML)
	public Response servers() {
		List<Server> result = new ArrayList<Server>();
		Map<String, Server> servers = clusterManager.getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			// TODO Clone these servers or everything
			// will fall apart because the actions are removed!
			Server server = mapEntry.getValue();
			server.setIndexContexts(null);
			List<Action> actions = server.getActions();
			for (Action action : actions) {
				action.setServer(null);
			}
			result.add(server);
		}
		return buildResponse(result);
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
	@SuppressWarnings("rawtypes")
	@Consumes(MediaType.APPLICATION_XML)
	public Response actions() {
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Server> servers = clusterManager.getServers();
		for (final Server server : servers.values()) {
			for (final Action action : server.getActions()) {
				IndexContext indexContext = getIndexContextFromServer(action.getIndexName(), server);
				Map<String, Object> actionData = new HashMap<String, Object>();
				action.setServer(null);
				addFieldValues(action, actionData);
				actionData.put("server", server.getAddress());
				if (indexContext != null && indexContext.getLastSnapshot() != null) {
					actionData.put("totalDocsIndexed", indexContext.getLastSnapshot().getNumDocs());
					actionData.put("docsPerMinute", indexContext.getLastSnapshot().getDocsPerMinute());
				}
				data.add(actionData);
			}
		}
		return buildResponse(data);
	}

	@GET
	@Path(Monitor.START)
	@Consumes(MediaType.APPLICATION_XML)
	public Response start(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
		long time = System.currentTimeMillis();
		Event startEvent = ListenerManager.getEvent(Event.STARTUP, time, indexName, Boolean.FALSE);
		logger.info("Sending start event : " + ToStringBuilder.reflectionToString(startEvent));
		clusterManager.sendMessage(startEvent);
		return buildResponse().build();
	}

	@GET
	@Path(Monitor.TERMINATE)
	@Consumes(MediaType.APPLICATION_XML)
	public Response terminate(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
		long time = System.currentTimeMillis();
		Event terminateEvent = ListenerManager.getEvent(Event.TERMINATE, time, indexName, Boolean.FALSE);
		clusterManager.sendMessage(terminateEvent);
		return buildResponse().build();
	}

	@GET
	@Path(Monitor.GET_PROPERTIES)
	@Consumes(MediaType.APPLICATION_XML)
	public Response getProperties() {
		return buildResponse(monitorService.getProperties());
	}

	@POST
	@Path(Monitor.SET_PROPERTIES)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response setProperties(@FormParam(value = IConstants.FILE) final String file,
			@FormParam(value = IConstants.CONTENTS) final String contents) {
		if (!StringUtils.isEmpty(file) && !StringUtils.isEmpty(contents)) {
			Map<String, String> filesAndProperties = new HashMap<String, String>();
			filesAndProperties.put(file, contents);
			monitorService.setProperties(filesAndProperties);
		}
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

	@SuppressWarnings("rawtypes")
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

	private Object[][] invertMatrix(Object[][] matrix) {
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

	private Double getDoubleTime(final int hour, final int minute) {
		return Double.parseDouble(hour + "." + minute);
	}

}