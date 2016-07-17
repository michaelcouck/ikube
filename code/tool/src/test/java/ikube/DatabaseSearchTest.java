package ikube;

import ikube.data.DatabaseSearch;
import org.junit.Test;
import org.mockito.Spy;

public class DatabaseSearchTest extends AbstractTest {

    @Spy
    private DatabaseSearch databaseSearch;

    @Test
    public void search() throws ClassNotFoundException {
        // Class.forName(OracleDriver.class.getName());
        Class.forName(oracle.jdbc.driver.OracleDriver.class.getName());
        String table = "auditrecord";
        String[] urls = {
                "jdbc:oracle:thin:@be-qa-coba-01:1521:BPH",
                "jdbc:oracle:thin:@be-qa-coba-02:1521:BPH",
                "jdbc:oracle:thin:@be-qa-coba-10:1521:BPH",
                "jdbc:oracle:thin:@be-qa-coba-11:1521:BPH",
                "jdbc:oracle:thin:@be-qa-coba-12:1521:BPH",
                "jdbc:oracle:thin:@be-qa-coba-13:1521:BPH",
                "jdbc:oracle:thin:@be-qa-coba-14:1521:BPH"
        };
        for (final String url : urls) {
            databaseSearch.search(url, "BPHADMIN", "password", table);
        }
    }

}