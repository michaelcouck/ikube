package ikube.experimental;

import com.jcraft.jsch.JSchException;
import ikube.database.IDataBase;
import ikube.model.Rule;
import ikube.toolkit.THREAD;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-07-2015
 */
@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:experimental/spring.xml"})
@SuppressWarnings({"SpringJavaAutowiringInspection", "SpringContextConfigurationInspection"})
public class ManagerIntegration {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    boolean shuttingDown;
    @Autowired
    @Qualifier("ikube.experimental.Manager")
    public Manager manager;
    @Autowired
    private IDataBase dataBase;

    @Before
    public void before() {
        THREAD.initialize();
    }

    @After
    public void after() {
        shuttingDown = Boolean.TRUE;
        THREAD.sleep(5000);
    }

    @Test
    public void process() throws SQLException, JSchException {
        addRules();
        addSearches();
        THREAD.sleep(5000 * 60);
    }

    private void addSearches() {
        class Searcher implements Runnable {
            public void run() {
                THREAD.sleep(30000);
                int counter = 0;
                do {
                    Rule rule = dataBase.find(Rule.class, 0, 1).get(0);
                    String id = Double.toString(rule.getId());
                    ArrayList<HashMap<String, String>> results = manager.doSearch("ID", id);
                    Assert.assertEquals("Must be two results, the hit and the statistics : ", 2, results.size());
                    if (counter++ % 1000 == 0) {
                        logger.info("Results : " + results);
                    }
                    THREAD.sleep(10);
                } while (!shuttingDown);
            }
        }
        THREAD.submit("searcher", new Searcher());
    }

    private void addRules() {
        class Persister implements Runnable {
            public void run() {
                long count;
                do {
                    long start = System.currentTimeMillis();
                    ArrayList<Rule> rules = new ArrayList<>();
                    for (int i = 0; i < 5000; i++) {
                        Rule rule = new Rule();
                        rule.setAction("action");
                        rule.setIndexContext("index-context");
                        rule.setPredicate("predicate");
                        rule.setServer("192.168.1.40");
                        rule.setTimestamp(new Timestamp(System.currentTimeMillis()));
                        rules.add(rule);
                    }

                    dataBase.persistBatch(rules);

                    count = dataBase.count(Rule.class);
                    long sleep = Math.abs((start + 1000) - System.currentTimeMillis());
                    if (count % 10000 == 0) {
                        logger.error("Rule count : " + count + ", sleeping for : " + sleep);
                    }
                    THREAD.sleep(sleep);
                } while (shuttingDown);
            }
        }
        THREAD.submit("rule-persister", new Persister());
    }

}