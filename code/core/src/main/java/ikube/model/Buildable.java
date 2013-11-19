package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * This class represents configuration and properties, and potentially logic that can build another object. For example the analyzers may need input in the form
 * of files, then this class will hold the properties that are necessary for the analyzer to be instanciated, initialized and trained.
 * 
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Buildable extends Persistable {

	private String type;
	private String filePath;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}