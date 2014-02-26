package ikube.model;

/**
 * This is the base class for objects that get distributed in the cluster, like searching and analysis,
 * for lateral scalability.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
public abstract class Distributed extends Persistable {

    /**
     * Whether this object will be distributed into the cluster for processing.
     */
    private boolean distributed;

    public boolean isDistributed() {
        return distributed;
    }

    public void setDistributed(boolean distributed) {
        this.distributed = distributed;
    }


}
