package ikube.web.service;

import ikube.IConstants;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public static final String SERVERS = "/servers";
	public static final String INDEXES = "/indexes";
	public static final String INDEXING = "/indexing";
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
	@Path(Monitor.INDEX_CONTEXT)
	@Consumes(MediaType.APPLICATION_XML)
	public Response indexContext(@QueryParam(value = IConstants.INDEX_NAME) final String indexName) {
		return buildResponse(monitorService.getIndexContext(indexName));
	}

	@GET
	@Path(Monitor.INDEX_CONTEXTS)
	@Consumes(MediaType.APPLICATION_XML)
	public Response indexContexts() {
		return buildResponse(monitorService.getIndexContexts());
	}

	@GET
	@Path(Monitor.SERVERS)
	@Consumes(MediaType.APPLICATION_XML)
	public Response servers() {
		List<Server> result = new ArrayList<Server>();
		Map<String, Server> servers = clusterManager.getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Consumes(MediaType.APPLICATION_XML)
	public Response indexingStatistics() {
		int snapshotSize = 0;
		Double[] snapshotTime = new Double[150];
		Map<String, Double[]> dataTable = new HashMap<String, Double[]>();
		Map<String, Server> servers = clusterManager.getServers();
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		for (final Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			Double[] docsPerMinute = dataTable.get(server.getAddress());

			List<IndexContext> indexContexts = server.getIndexContexts();
			for (final IndexContext indexContext : indexContexts) {
				List<Snapshot> snapshots = indexContext.getSnapshots();
				snapshotSize = snapshots.size();

				if (docsPerMinute == null) {
					docsPerMinute = new Double[snapshotSize];
					Arrays.fill(docsPerMinute, 0d);
					dataTable.put(server.getAddress(), docsPerMinute);
				}

				int index = 0;
				for (final Snapshot snapshot : snapshots) {
					gregorianCalendar.setTime(snapshot.getTimestamp());
					int hour = gregorianCalendar.get(Calendar.HOUR_OF_DAY);
					int minute = gregorianCalendar.get(Calendar.MINUTE);
					snapshotTime[index] = getDoubleTime(hour, minute);
					if (docsPerMinute.length <= index) {
						continue;
					}
					docsPerMinute[index++] += snapshot.getDocsPerMinute();
				}

				// logger.info("Server : " + server.getAddress() + ", " + Arrays.deepToString(docsPerMinute));
			}
		}
		int i = 0;
		Double[][] matrix = new Double[servers.size()][snapshotSize];
		for (final Map.Entry<String, Double[]> mapEntry : dataTable.entrySet()) {
			matrix[i++] = mapEntry.getValue();
		}
		Double[][] invertedMatrix = invertMatrix(matrix);
		// logger.info(Arrays.deepToString(matrix));
		// logger.info(Arrays.deepToString(intervedMatrix));
		Object[][] results = new Object[snapshotSize + 1][servers.size() + 1];

		results[0][0] = "Time";

		// The top row is the time header and the server addresses
		// it would look like 'Time, 192.168.1.4, 192.168.1.6, 192.168.1.7'
		i = 1;
		for (final Server server : servers.values()) {
			results[0][i++] = server.getAddress();
		}

		// Add the matrix data to the results
		i = 1;
		for (Double[] matrixRow : invertedMatrix) {
			results[i][0] = snapshotTime[i - 1];
			System.arraycopy(matrixRow, 0, results[i++], 1, matrixRow.length);
		}

		return buildResponse(results);
	}

	private Double[][] invertMatrix(Double[][] matrix) {
		final int m = matrix.length;
		final int n = matrix[0].length;
		Double[][] inverted = new Double[n][m];
		for (int r = 0; r < m; r++) {
			for (int c = 0; c < n; c++) {
				inverted[c][m - 1 - r] = matrix[r][c];
			}
		}
		return inverted;
	}

	private double getDoubleTime(final int hour, final int minute) {
		return Double.parseDouble(hour + "." + minute);
	}

	private double addDoubleTime(final double first, final int hour, final int minute) {
		return first + getDoubleTime(hour, minute);
	}
}