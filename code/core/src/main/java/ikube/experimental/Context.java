package ikube.experimental;

import java.sql.Timestamp;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 14-08-2015
 */
public class Context {

    private String name;
    // The last timestamp for data that was indexed
    private Timestamp modification = new Timestamp(0);

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Timestamp getModification() {
        return modification;
    }

    public void setModification(final Timestamp modification) {
        this.modification = modification;
    }
}
