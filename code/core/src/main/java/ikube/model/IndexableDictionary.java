package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class IndexableDictionary extends Indexable<IndexableDictionary> {

	private String	path;

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

}
