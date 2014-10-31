package ikube.mock;

import ikube.database.DatabaseUtilities;
import mockit.Mock;
import mockit.MockClass;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-11-2010
 */
@SuppressWarnings({"UnusedParameters", "UnusedDeclaration"})
@MockClass(realClass = DatabaseUtilities.class)
public class DatabaseUtilitiesMock {

    @Mock()
    public static List<String> getAllColumns(final Connection connection, final String table) {
        return Arrays.asList("id", "name", "address");
    }

    @Mock()
    public static List<String> getPrimaryKeys(final Connection connection, final String table) {
        return Arrays.asList("id");
    }

}
