package ikube.experimental;

import com.jcraft.jsch.JSchException;
import ikube.database.IDataBase;
import ikube.experimental.search.Searcher;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-07-2015
 */
@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:experimental/spring.xml"})
@SuppressWarnings({"SpringJavaAutowiringInspection", "SpringContextConfigurationInspection"})
public class Integration {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    boolean shuttingDown;

    @Autowired
    private Searcher searcher;
    @Autowired
    private IDataBase dataBase;

    @Before
    public void before() {
        THREAD.initialize();
        dataBase.removeBatch(dataBase.find(Rule.class, 0, Integer.MAX_VALUE));
    }

    @After
    public void after() {
        dataBase.removeBatch(dataBase.find(Rule.class, 0, Integer.MAX_VALUE));
        shuttingDown = Boolean.TRUE;
        THREAD.sleep(30000);
    }

    @Test
    public void process() throws SQLException, JSchException {
        addRules();
        addSearches();
        THREAD.sleep(1000 * 6000);
    }

    private void addSearches() {
        class Searcher implements Runnable {
            public void run() {
                THREAD.sleep(30000);
                int counter = 0;
                do {
                    Rule rule = dataBase.find(Rule.class, 0, 1).get(0);
                    String id = Double.toString(rule.getId());
                    ArrayList<HashMap<String, String>> results = searcher.doSearch("ID", id);
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
                Random random = new Random();
                do {
                    int insertsPerSecond = random.nextInt(100);
                    long start = System.currentTimeMillis();
                    ArrayList<Rule> rules = new ArrayList<>();
                    for (int i = 0; i < insertsPerSecond; i++) {
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
                    if (count % 1000 == 0) {
                        logger.info("Rule count : {}, sleeping for : {}", count, sleep);
                    }
                    THREAD.sleep(sleep);
                } while (!shuttingDown);
            }
        }
        THREAD.submit("rule-persister", new Persister());
    }

}