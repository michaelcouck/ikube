package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * This is the base class for objects that get distributed in the cluster, like searching and analysis,
 * for lateral scalability.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
@Entity
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Distributed extends Persistable {

    /**
     * Whether this object will be distributed into the cluster for processing.
     */
    @Column
    private boolean distributed;

    public boolean isDistributed() {
        return distributed;
    }

    public void setDistributed(boolean distributed) {
        this.distributed = distributed;
    }


}
