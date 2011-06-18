package ikube.service;

import ikube.ATest;
import ikube.cluster.ClusterManager;
import ikube.cluster.cache.Cache;
import ikube.mock.ClusterManagerMock;
import mockit.Mockit;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * This is the base test for the service tests.
 * 
 * @author Michael Couck
 * @since 29.05.11
 * @version 01.00
 */
public abstract class AServiceTest extends ATest {

	@BeforeClass
	public static void beforeClass() {
		Mockit.setUpMock(ClusterManagerMock.class);
	}

	@AfterClass
	public static void afterClass() {
		Mockit.tearDownMocks();
	}

	protected WebServicePublisher webServicePublisher;

	public AServiceTest(Class<?> klass) {
		super(klass);
	}

	@Before
	public void before() throws Exception {
		webServicePublisher = new WebServicePublisher(new ClusterManager(new Cache()));
		webServicePublisher.postProcessAfterInitialization(new MonitorWebService(), MonitorWebService.class.getSimpleName());
		webServicePublisher.postProcessAfterInitialization(new SearcherWebService(), SearcherWebService.class.getSimpleName());
	}

}
