package ikube.service;

import ikube.ATest;
import ikube.cluster.ClusterManager;
import ikube.cluster.cache.Cache;
import ikube.mock.ClusterManagerMock;

import java.util.Arrays;
import java.util.List;

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

	protected int port = 9010;
	protected WebServicePublisher webServicePublisher;

	public AServiceTest(Class<?> klass) {
		super(klass);
	}

	@Before
	@SuppressWarnings("unchecked")
	public void before() throws Exception {
		webServicePublisher = new WebServicePublisher(new ClusterManager(new Cache()));
		List<?> implementors = Arrays.asList(new SearcherWebService());
		webServicePublisher.setImplementors((List<Object>) implementors);
		webServicePublisher.setPaths(Arrays.asList(ISearcherWebService.PUBLISHED_PATH));
		webServicePublisher.setPorts(Arrays.asList(port));
		webServicePublisher.setProtocols(Arrays.asList("http"));
	}

}
