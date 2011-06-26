package ikube.monitoring;

import ikube.cluster.IClusterManager;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.Execution;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.SerializationUtilities;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @see IMonitoringInterceptor
 * @author Michael Couck
 * @since 08.05.2011
 * @version 01.00
 */
public class MonitoringInterceptor implements IMonitoringInterceptor, IListener {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(MonitoringInterceptor.class);

	protected transient final Map<String, Execution> indexingExecutions;
	protected transient final Map<String, Execution> searchingExecutions;

	public MonitoringInterceptor() {
		ListenerManager.addListener(this);
		this.indexingExecutions = new HashMap<String, Execution>();
		this.searchingExecutions = new HashMap<String, Execution>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object indexingPerformance(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		Object[] args = proceedingJoinPoint.getArgs();
		String indexName = ((IndexContext<?>) args[0]).getIndexName();
		Execution execution = getExecution(indexName, indexingExecutions);
		long start = System.nanoTime();
		try {
			return proceedingJoinPoint.proceed();
		} finally {
			execution.duration += System.nanoTime() - start;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object searchingPerformance(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// We expect to have the SearcherWebService, and nothing else for the time being
		Object[] args = proceedingJoinPoint.getArgs();
		String indexName = (String) args[0];
		Execution execution = getExecution(indexName, searchingExecutions);
		long start = System.nanoTime();
		try {
			return proceedingJoinPoint.proceed();
		} finally {
			execution.duration += System.nanoTime() - start;
		}
	}

	protected Execution getExecution(String name, Map<String, Execution> executions) {
		Execution execution = executions.get(name);
		if (execution == null) {
			execution = new Execution();
			execution.name = name;
			executions.put(execution.name, execution);
		}
		execution.invocations++;
		return execution;
	}

	@Override
	public Map<String, Execution> getIndexingExecutions() {
		return indexingExecutions;
	}

	@Override
	public Map<String, Execution> getSearchingExecutions() {
		return searchingExecutions;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handleNotification(Event event) {
		// LOGGER.info("Monitoring interceptor : " + event);
		if (event.getType().equals(Event.PERFORMANCE)) {
			// Get the server
			IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
			Server server = clusterManager.getServer();
			calculateStatistics(indexingExecutions);
			calculateStatistics(searchingExecutions);
			server.setIndexingExecutions((Map<String, Execution>) SerializationUtilities.clone(indexingExecutions));
			server.setSearchingExecutions((Map<String, Execution>) SerializationUtilities.clone(searchingExecutions));
			// LOGGER.info("Publishing server : " + server);
			// Publish the server with the new data
			clusterManager.set(Server.class.getName(), server.getId(), server);
		}
	}

	private void calculateStatistics(Map<String, Execution> executions) {
		for (Execution execution : executions.values()) {
			long duration = TimeUnit.NANOSECONDS.toSeconds(execution.duration);
			if (duration > 0) {
				execution.executionsPerSecond = execution.invocations / duration;
			}
		}
	}

}