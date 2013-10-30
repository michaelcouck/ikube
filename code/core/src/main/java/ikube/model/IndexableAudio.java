package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableAudio extends Indexable<IndexableAudio> {

	@Column
	private boolean filePath;

	public boolean isFilePath() {
		return filePath;
	}

	public void setFilePath(boolean filePath) {
		this.filePath = filePath;
	}

}