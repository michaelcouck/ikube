package ikube.experimental;

import ikube.database.IDataBase;
import ikube.model.geospatial.GeoName;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.THREAD;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This test is to check the locking and exception handling of JPA based on the locking levels
 * that are adopted. There was some issue with the syntax of the SQL generated, and so this test is not
 * conclusive.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-07-2015
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings("SpringJavaAutowiringInspection")
@ContextConfiguration(locations = {"file:ikube/spring.xml"})
public class RaceServiceIntegration {

    @Autowired
    private RaceService raceService;

    @Before
    public void before() {
        THREAD.initialize();
    }

    @After
    public void after() {
        THREAD.destroy();
    }

    @Test
    public void update() {
        RaceService localRaceService = ApplicationContextManager.getBean(RaceService.class);
        Assert.assertEquals(raceService, localRaceService);
        IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
        raceService.setDataBase(dataBase);

        final long id = raceService.persist();

        THREAD.submit("race-service", new Runnable() {
            public void run() {
                try {
                    raceService.update(id);
                } finally {
                    THREAD.destroy("race-service");
                }
            }
        });

        // Wait for the service to get a reference to the geoname
        THREAD.sleep(500);
        // Update the geoname in another transaction
        raceService.intercept(id);
        // And wait for the service to try to update the now out of date geoname
        THREAD.sleep(1000);
        // And the future should have thrown an exception
        System.out.println(ToStringBuilder.reflectionToString(dataBase.find(GeoName.class, id)));
    }

}