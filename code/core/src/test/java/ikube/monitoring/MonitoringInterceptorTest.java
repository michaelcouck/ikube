package ikube.monitoring;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.Execution;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.PerformanceTester;
import mockit.Deencapsulation;
import mockit.Mockit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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

	@BeforeClass
	public static void beforeClass() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class);
	}

	@AfterClass
	public static void afterClass() {
		Mockit.tearDownMocks(ApplicationContextManager.class);
	}

	public MonitoringInterceptorTest() {
		super(MonitoringInterceptorTest.class);
	}

	@Before
	public void before() {
		signature = mock(Signature.class);
		proceedingJoinPoint = mock(ProceedingJoinPoint.class);
		when(proceedingJoinPoint.getSignature()).thenReturn(signature);
		monitoringInterceptor = new MonitoringInterceptor();
		Deencapsulation.setField(monitoringInterceptor, dataBase);
		Deencapsulation.setField(monitoringInterceptor, clusterManager);
	}

	@Test
	public void indexingPerformance() throws Throwable {
		arguments = new Object[] { indexContext };
		when(proceedingJoinPoint.getArgs()).thenReturn(arguments);

		monitoringInterceptor.indexingPerformance(proceedingJoinPoint);
		logger.info(monitoringInterceptor.indexingExecutions);
		assertTrue(monitoringInterceptor.indexingExecutions.size() == 1);
		assertTrue(monitoringInterceptor.indexingExecutions.containsKey(indexContext.getIndexName()));
		Execution execution = monitoringInterceptor.indexingExecutions.get(indexContext.getIndexName());
		assertTrue(execution.getInvocations() > 0);
		// Performance test
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				monitoringInterceptor.indexingPerformance(proceedingJoinPoint);
			}
		}, "Indexing interceptor : ", 100, Boolean.FALSE);
		assertTrue(executionsPerSecond > 100);
	}

	@Test
	public void searchingPerformance() throws Throwable {
		arguments = new Object[] { indexContext.getIndexName() };
		when(proceedingJoinPoint.getArgs()).thenReturn(arguments);

		monitoringInterceptor.searchingPerformance(proceedingJoinPoint);
		logger.info(monitoringInterceptor.searchingExecutions);
		assertTrue(monitoringInterceptor.searchingExecutions.size() == 1);
		assertTrue(monitoringInterceptor.searchingExecutions.containsKey(arguments[0]));
		Execution execution = monitoringInterceptor.searchingExecutions.get(arguments[0]);
		assertTrue(execution.getInvocations() > 0);
		// Performance test
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				monitoringInterceptor.searchingPerformance(proceedingJoinPoint);
			}
		}, "Indexing interceptor : ", 100, Boolean.FALSE);
		assertTrue(executionsPerSecond > 100);
	}

}
