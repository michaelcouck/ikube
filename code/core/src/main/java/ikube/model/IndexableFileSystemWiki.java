package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

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
	@Min(value = 1)
	@Max(value = Integer.MAX_VALUE)
	@Attribute(field = false, description = "This is the maximum documents that will be read from the source before the indexing terminates")
	private long maxRevisions;

	public long getMaxRevisions() {
		return maxRevisions;
	}

	public void setMaxRevisions(long maxRevisions) {
		this.maxRevisions = maxRevisions;
	}

}