package ikube.monitoring;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.model.Execution;
import ikube.model.IndexContext;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * TODO Start a timer to persist the executions periodically.
 * 
 * @see IMonitoringInterceptor
 * @author Michael Couck
 * @since 08.05.2011
 * @version 01.00
 */
public class MonitoringInterceptor implements IMonitoringInterceptor, IListener {

	private static final Logger			LOGGER	= Logger.getLogger(MonitoringInterceptor.class);

	private IDataBase					dataBase;
	protected Map<String, Execution>	indexingExecutions;
	protected Map<String, Execution>	searchingExecutions;

	public MonitoringInterceptor() {
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
		Execution execution = getExecution(indexName, IConstants.INDEX, indexingExecutions);
		long start = System.nanoTime();
		try {
			return proceedingJoinPoint.proceed();
		} finally {
			execution.setDuration(execution.getDuration() + System.nanoTime() - start);
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
		Execution execution = getExecution(indexName, IConstants.SEARCH, searchingExecutions);
		long start = System.nanoTime();
		try {
			return proceedingJoinPoint.proceed();
		} finally {
			execution.setDuration(execution.getDuration() + System.nanoTime() - start);
		}
	}

	@Override
	public void handleNotification(Event event) {
		persistExecutions(indexingExecutions);
		persistExecutions(searchingExecutions);
	}

	protected void persistExecutions(Map<String, Execution> executions) {
		try {
			for (Map.Entry<String, Execution> entry : executions.entrySet()) {
				dataBase.persist(entry.getValue());
			}
			executions.clear();
		} catch (Exception e) {
			LOGGER.error("Exception persisting the executions : ", e);
		}
	}

	protected Execution getExecution(String name, String type, Map<String, Execution> executions) {
		Execution execution = executions.get(name);
		if (execution == null) {
			execution = new Execution();
			execution.setName(name);
			execution.setType(type);
			executions.put(execution.getName(), execution);
		}
		execution.setInvocations(execution.getInvocations() + 1);
		return execution;
	}

	public void setDataBase(IDataBase dataBase) {
		this.dataBase = dataBase;
	}

}