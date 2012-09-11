package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * This class contains one file, a Bzip2 file for indexing using the Wiki handler.
 * 
 * @author Michael Couck
 * @since 19.05.12
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableFileSystemWiki extends IndexableFileSystem {

	@Column
	private long maxRevisions;

	public long getMaxRevisions() {
		return maxRevisions;
	}

	public void setMaxRevisions(long maxRevisions) {
		this.maxRevisions = maxRevisions;
	}

}