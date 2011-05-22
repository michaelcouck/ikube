package ikube.monitoring;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.cluster.IClusterManager;
import ikube.listener.Event;
import ikube.model.Execution;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.PerformanceTester;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 22.05.2011
 * @version 01.00
 */
public class MonitoringInterceptorTest extends ATest {

	private Signature signature;
	private ProceedingJoinPoint proceedingJoinPoint;
	private MonitoringInterceptor monitoringInterceptor;
	private Object[] arguments;

	public MonitoringInterceptorTest() {
		super(MonitoringInterceptorTest.class);
	}

	@Before
	public void before() {
		signature = mock(Signature.class);
		proceedingJoinPoint = mock(ProceedingJoinPoint.class);
		when(proceedingJoinPoint.getSignature()).thenReturn(signature);
		monitoringInterceptor = new MonitoringInterceptor();
	}

	@Test
	public void indexingPerformance() throws Throwable {
		arguments = new Object[] { INDEX_CONTEXT };
		when(proceedingJoinPoint.getArgs()).thenReturn(arguments);

		monitoringInterceptor.indexingPerformance(proceedingJoinPoint);
		logger.info(monitoringInterceptor.indexingExecutions);
		assertTrue(monitoringInterceptor.indexingExecutions.size() == 1);
		assertTrue(monitoringInterceptor.indexingExecutions.containsKey(INDEX_CONTEXT.getIndexName()));
		Execution execution = monitoringInterceptor.indexingExecutions.get(INDEX_CONTEXT.getIndexName());
		assertTrue(execution.invocations > 0);
		// Performance test
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				monitoringInterceptor.indexingPerformance(proceedingJoinPoint);
			}
		}, "Indexing interceptor : ", 10000, Boolean.FALSE);
		assertTrue(executionsPerSecond > 1000);
	}

	@Test
	public void searchingPerformance() throws Throwable {
		arguments = new Object[] { INDEX_CONTEXT.getIndexName() };
		when(proceedingJoinPoint.getArgs()).thenReturn(arguments);

		monitoringInterceptor.searchingPerformance(proceedingJoinPoint);
		logger.info(monitoringInterceptor.searchingExecutions);
		assertTrue(monitoringInterceptor.searchingExecutions.size() == 1);
		assertTrue(monitoringInterceptor.searchingExecutions.containsKey(arguments[0]));
		Execution execution = monitoringInterceptor.searchingExecutions.get(arguments[0]);
		assertTrue(execution.invocations > 0);
		// Performance test
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				monitoringInterceptor.searchingPerformance(proceedingJoinPoint);
			}
		}, "Indexing interceptor : ", 10000, Boolean.FALSE);
		assertTrue(executionsPerSecond > 1000);
	}

	@Test
	public void publishPerformanceData() throws Throwable {
		final IMonitoringInterceptor monitoringInterceptor = ApplicationContextManager.getBean(IMonitoringInterceptor.class);
		arguments = new Object[] { INDEX_CONTEXT.getIndexName() };
		when(proceedingJoinPoint.getArgs()).thenReturn(arguments);
		PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				monitoringInterceptor.searchingPerformance(proceedingJoinPoint);
			}
		}, "Indexing interceptor : ", 10000, Boolean.FALSE);

		arguments = new Object[] { INDEX_CONTEXT };
		when(proceedingJoinPoint.getArgs()).thenReturn(arguments);
		PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				monitoringInterceptor.indexingPerformance(proceedingJoinPoint);
			}
		}, "Indexing interceptor : ", 10000, Boolean.FALSE);

		Event event = mock(Event.class);
		when(event.getType()).thenReturn(Event.TIMER);
		monitoringInterceptor.handleNotification(event);
		// Verify that the server in the cluster manager has the new data
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		Server server = clusterManager.getServer();
		assertTrue("There should be some executions in the profiling data : ", server.getSearchingExecutions().size() > 0);
		assertTrue("There should be some executions in the profiling data : ", server.getIndexingExecutions().size() > 0);
	}

}
