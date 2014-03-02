package ikube.model;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-09-2012
 */
public class SearchIncrementListener {

    @PreUpdate
    @PrePersist
    public void prePersist(final Search search) {
        int count = search.getCount();
        count++;
        search.setCount(count);
    }

}
