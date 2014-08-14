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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

/**
 * The resource is the API interface for actions and statistics from the server, like terminating
 * an action or disabling the cpu throttling. Also for example getting and generating the indexing
 * data for the servers dynamically.
 *
 * @author Michael couck
 * @version 01.00
 * @since 16-10-2012
 */
@Component
@Path(Monitor.MONITOR)
@Scope(Monitor.REQUEST)
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
@Api(description = "The monitoring rest resource")
public class Monitor extends Resource {

    /**
     * Constants for the paths to the web services.
     */
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

    @GET
    @Path(Monitor.FIELDS)
    @Api(type = "GET",
            uri = "/ikube/service/monitor/fields",
            description ="This method will return the names of the Lucene fields in an index.",
            consumes = String.class,
            produces = String[].class)
    public Response fields(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
        if (StringUtils.isEmpty(indexName)) {
            return buildResponse(new String[0]);
        }
        return buildResponse(monitorService.getIndexFieldNames(indexName));
    }

    @GET
    @Path(Monitor.INDEXES)
    @Api(type = "GET",
            uri = "/ikube/service/monitor/indexes",
            description ="This method will return the names of all the indexes that are defined in the system/instance, this " +
                    "will be the default indexes like autocomplete and the user defined indexes.",
            consumes = Void.class,
            produces = String[].class)
    public Response indexes() {
        return buildResponse(monitorService.getIndexNames());
    }

    @GET
    @Path(Monitor.INDEX_CONTEXT)
    @Api(type = "GET",
            uri = "/ikube/service/monitor/index-context",
            description ="This method will return an index context, which is a holder for an inddex/collection. Please " +
                    "refer to the documentation for the definition of a context and an index and how to configure one in " +
                    "Spring.",
            consumes = Void.class,
            produces = IndexContext.class)
    public Response indexContext(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
        IndexContext indexContext = cloneIndexContext(monitorService.getIndexContext(indexName));
        return buildResponse(indexContext);
    }

    @GET
    @Path(Monitor.INDEX_CONTEXTS)
    @Api(type = "GET",
            uri = "/ikube/service/monitor/index-contexts",
            description ="This method will return a collection of all the index contexts that are defined in the " +
                    "system, sorted by a user defined field, similar to the above.",
            consumes = String.class,
            produces = ArrayList.class)
    public Response indexContexts(
            @QueryParam(value = IConstants.SORT_FIELD) final String sortField,
            @QueryParam(value = IConstants.DESCENDING) final boolean descending) {
        List<IndexContext> indexContexts = new ArrayList<>();
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
        return buildResponse(indexContexts);
    }

    @GET
    @Path(Monitor.SERVER)
    @Api(type = "GET",
            uri = "/ikube/service/monitor/server",
            description ="This method will return the local server object, which holds information on the server, the " +
                    "contexts defined, the snapshots of the system etc.",
            consumes = Void.class,
            produces = Server.class)
    public Response server() {
        Server server = clusterManager.getServer();
        Server cloneServer = cloneServer(server);
        return buildResponse(cloneServer);
    }

    @GET
    @Path(Monitor.SERVERS)
    @Api(type = "GET",
            uri = "/ikube/service/monitor/servers",
            description ="Similar to the above, this method will return all the servers in the cluster.",
            consumes = Void.class,
            produces = ArrayList.class)
    public Response servers() {
        List<Server> result = new ArrayList<>();
        Map<String, Server> servers = clusterManager.getServers();
        for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
            Server server = mapEntry.getValue();
            Server cloneServer = cloneServer(server);
            result.add(cloneServer);
        }
        return buildResponse(result);
    }

    @GET
    @Path(Monitor.INDEXING)
    @SuppressWarnings({"UnnecessaryBoxing", "unchecked"})
    @Api(type = "GET",
            uri = "/ikube/service/monitor/indexing",
            description ="This method will return the indexing statistics for the local server, in a matrix " +
                    "with the documents per minute and the time line for the indexing of the local server.",
            consumes = Void.class,
            produces = String.class)
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
        Object[][] invertedData = invertMatrix(data);
        String stringified = Arrays.deepToString(invertedData);
        return buildResponse(stringified);
    }

    @GET
    @Path(Monitor.SEARCHING)
    @Api(type = "GET",
            uri = "/ikube/service/monitor/searching",
            description ="This method will return the searching statistics for the local server, also a matrix " +
                    "of searches per minute and time line, converted into a string for easier Json rendering and graph " +
                    "creation using JavaScript.",
            consumes = Void.class,
            produces = String.class)
    @SuppressWarnings({"UnnecessaryBoxing", "unchecked"})
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
        Object[][] invertedData = invertMatrix(data);
        String stringified = Arrays.deepToString(invertedData);
        return buildResponse(stringified);
    }

    @GET
    @Path(Monitor.ACTIONS)
    @Api(type = "GET",
            uri = "/ikube/service/monitor/actions",
            description ="This method will return the actions that are currently being executed in the " +
                    "whole cluster, i.e. all the action running on all the servers.",
            consumes = Void.class,
            produces = String.class)
    public Response actions() {
        List<Action> clonedActions = new ArrayList<>();
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
        return buildResponse(clonedActions);
    }

    @POST
    @Path(Monitor.START)
    @Consumes(MediaType.APPLICATION_JSON)
    @Api(type = "POST",
            uri = "/ikube/service/monitor/start",
            description ="This method will start and index running, setting the maximum age to zero, and so " +
                    "triggering the logic to start the job.",
            consumes = String.class,
            produces = String.class)
    public Response start(final String indexName) {
        monitorService.start(indexName);
        return buildResponse(null);
    }

    @POST
    @Path(Monitor.TERMINATE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Api(type = "POST",
            uri = "/ikube/service/monitor/terminate",
            description ="This method will terminate a job, by terminating the threads that the job has spawned. This " +
                    "may not always terminate all threads as Java does not support thread termination directly, you have " +
                    "to ask for the threads to terminate naturally.",
            consumes = String.class,
            produces = String.class)
    public Response terminate(final String indexName) {
        monitorService.terminate(indexName);
        return buildResponse(null);
    }

    @GET
    @Path(Monitor.GET_PROPERTIES)
    @Consumes(MediaType.APPLICATION_JSON)
    @Api(type = "GET",
            uri = "/ikube/service/monitor/get-properties",
            description ="This method will get all the properties that are defined in the properties " +
                    "files for this instance, i.e. the local server.",
            consumes = Void.class,
            produces = HashMap.class)
    public Response getProperties() {
        return buildResponse(monitorService.getProperties());
    }

    @POST
    @SuppressWarnings("unchecked")
    @Path(Monitor.SET_PROPERTIES)
    @Consumes(MediaType.APPLICATION_JSON)
    @Api(type = "POST",
            uri = "/ikube/service/monitor/set-properties",
            description ="This method will set the properties for the application, taking a map of properties and " +
                    "replacing them by the ones in the method signature. Writing the properties to the properties files " +
                    "on the file system.",
            consumes = HashMap.class,
            produces = String.class)
    public Response setProperties(
            @Context final HttpServletRequest request) throws IOException {
        Map<String, String> filesAndProperties = unmarshall(Map.class, request);
        monitorService.setProperties(filesAndProperties);
        return buildResponse(null);
    }

    @POST
    @Path(Monitor.TERMINATE_ALL)
    @Consumes(MediaType.APPLICATION_JSON)
    @Api(type = "GET",
            uri = "/ikube/service/monitor/terminate-all",
            description ="This method will terminate all the jobs in teh system. Essentially by destroying " +
                    "all the thread pools and the underlying threads in them.",
            consumes = Void.class,
            produces = String.class)
    public Response terminateAll() {
        monitorService.terminateAll();
        return buildResponse(null);
    }

    @POST
    @Path(Monitor.STARTUP_ALL)
    @Consumes(MediaType.APPLICATION_JSON)
    @Api(type = "POST",
            uri = "/ikube/service/monitor/startup-all",
            description ="This method will start all the thread pools in teh application, and thereby start the " +
                    "jobs and schedules too.",
            consumes = Void.class,
            produces = String.class)
    public Response startupAll() {
        monitorService.startupAll();
        return buildResponse(null);
    }

    @POST
    @Path(Monitor.DELETE_INDEX)
    @Consumes(MediaType.APPLICATION_JSON)
    @Api(type = "POST",
            uri = "/ikube/service/monitor/delete-index",
            description ="This method will delete the file system index that is specified, including any backup " +
                    "index that has been written to the file system. This method is quite brutal, the index will not " +
                    "be closed, it will just be deleted.",
            consumes = Void.class,
            produces = String.class)
    public Response delete(final String indexName) {
        long time = System.currentTimeMillis();
        Event startEvent = IListener.EventGenerator.getEvent(Event.DELETE_INDEX, time, indexName, Boolean.FALSE);
        logger.info("Sending delete event : " + ToStringBuilder.reflectionToString(startEvent));
        clusterManager.sendMessage(startEvent);
        return buildResponse(null);
    }

    @POST
    @Path(Monitor.CPU_THROTTLING)
    @Consumes(MediaType.APPLICATION_JSON)
    @Api(type = "GET",
            uri = "/ikube/service/monitor/cpu-throttling",
            description ="This method will toggle the cpu throttling functionality. This will slow down the processing " +
                    "when the cpu exceeds a certain threshold per core, defined by the user.",
            consumes = Void.class,
            produces = String.class)
    public Response cpuThrottling() {
        monitorService.cpuThrottling();
        return buildResponse(null);
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

    @SuppressWarnings("unchecked")
    private IndexContext cloneIndexContext(
            final IndexContext indexContext) {
        IndexContext cloneIndexContext = (IndexContext) SerializationUtilities.clone(indexContext);
        cloneIndexContext.setChildren(null);
        // cloneIndexContext.setSnapshots(null);
        return cloneIndexContext;
    }

    @SuppressWarnings({"LoopStatementThatDoesntLoop", "unchecked"})
    private Object[] getTimes(final Map<String, Server> servers, final ArrayList<Object> times) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        outer:
        for (final Map.Entry<String, Server> mapEntry : servers.entrySet()) {
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