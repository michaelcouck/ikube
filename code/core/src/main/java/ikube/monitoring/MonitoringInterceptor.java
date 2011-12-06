package ikube.monitoring;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.model.Execution;
import ikube.model.IndexContext;
import ikube.toolkit.SerializationUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO Start a timer to persist the executions periodically.
 * 
 * @see IMonitoringInterceptor
 * @author Michael Couck
 * @since 08.05.2011
 * @version 01.00
 */
public class MonitoringInterceptor implements IMonitoringInterceptor, IListener {

	private static final Logger LOGGER = Logger.getLogger(MonitoringInterceptor.class);

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private IClusterManager clusterManager;
	protected final Map<String, Execution> indexingExecutions;
	protected final Map<String, Execution> searchingExecutions;

	public MonitoringInterceptor() {
		this.indexingExecutions = new HashMap<String, Execution>();
		this.searchingExecutions = new HashMap<String, Execution>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Object indexingPerformance(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		Object[] args = proceedingJoinPoint.getArgs();
		String indexName = ((IndexContext<?>) args[0]).getIndexName();
		long start = System.nanoTime();
		try {
			return proceedingJoinPoint.proceed();
		} finally {
			Execution execution = getExecution(indexName, IConstants.INDEX, indexingExecutions);
			execution.setInvocations(execution.getInvocations() + 1);
			execution.setDuration(execution.getDuration() + (System.nanoTime() - start));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Object searchingPerformance(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// We expect to have the SearcherWebService, and nothing else for the time being
		Object[] args = proceedingJoinPoint.getArgs();
		String indexName = (String) args[0];
		long start = System.nanoTime();
		try {
			return proceedingJoinPoint.proceed();
		} finally {
			Execution execution = getExecution(indexName, IConstants.SEARCH, searchingExecutions);
			execution.setInvocations(execution.getInvocations() + 1);
			execution.setDuration(execution.getDuration() + (System.nanoTime() - start));
		}
	}

	protected final Execution getExecution(final String indexName, final String type, final Map<String, Execution> executions) {
		Execution execution = executions.get(indexName);
		// Try find it in the database
		if (execution == null) {
			try {
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("indexName", indexName);
				parameters.put("type", type);
				parameters.put("address", clusterManager.getServer().getAddress());
				List<Execution> dbExecutions = dataBase.find(Execution.class, Execution.SELECT_FROM_EXECUTIONS_BY_NAME_TYPE_AND_ADDRESS,
						parameters, 0, Integer.MAX_VALUE);
				if (dbExecutions.size() == 0) {
					LOGGER.info("Persisting execution : ");
				} else if (dbExecutions.size() == 1) {
					execution = dbExecutions.get(0);
				} else {
					LOGGER.warn("Using the same database in a cluster?");
					execution = dbExecutions.get(0);
				}
			} catch (Exception e) {
				LOGGER.info("No result for execution : " + e.getMessage());
			}
			if (execution == null) {
				execution = new Execution();
				execution.setIndexName(indexName);
				execution.setType(type);
				executions.put(execution.getIndexName(), execution);
				dataBase.persist(SerializationUtilities.clone(execution));
				if (IConstants.SEARCHING_EXECUTIONS.equals(type)) {
					clusterManager.getServer().getSearchingExecutions().put(indexName, execution);
				} else {
					clusterManager.getServer().getIndexingExecutions().put(indexName, execution);
				}
			}
		}
		return execution;
	}

	@Override
	public final void handleNotification(Event event) {
		// persistExecutions(indexingExecutions);
		// persistExecutions(searchingExecutions);
	}

	protected final void persistExecutions(Map<String, Execution> executions) {
		try {
			Collection<Execution> clonedValues = new ArrayList<Execution>();
			clonedValues.addAll(executions.values());
			executions.clear();
			for (Execution execution : clonedValues) {
				dataBase.persist(execution);
			}
		} catch (Exception e) {
			LOGGER.error("Exception persisting the executions : ", e);
		}
	}

}