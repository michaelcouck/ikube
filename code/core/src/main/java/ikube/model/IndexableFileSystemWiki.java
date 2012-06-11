package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * This class contains one file, a Bzip2 file for indexing uwing the Wiki handler.
 * 
 * @author Michael Couck
 * @since 19.05.12
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class IndexableFileSystemWiki extends IndexableFileSystem {

	private int maxRevisions;

	public int getMaxRevisions() {
		return maxRevisions;
	}

	public void setMaxRevisions(int maxRevisions) {
		this.maxRevisions = maxRevisions;
	}

}