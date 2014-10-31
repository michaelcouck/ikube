package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.concurrent.Callable;

/**
 * This class is the base class for callables that are distributed over the grid.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 25-02-2014
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Task extends Persistable implements Callable {

    /**
     * {@inheritDoc}
     */
    @Override
    public Object call() throws Exception {
        return Boolean.TRUE;
    }
}