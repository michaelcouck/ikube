package ikube.model;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

/**
 * @author Michael Couck
 * @since 29.09.12
 * @version 01.00
 */
public class SearchIncrementListener {

	@PrePersist
	@PreUpdate
	public void prePersist(final Search search) {
		int count = search.getCount();
		count++;
		search.setCount(count);
	}

}
