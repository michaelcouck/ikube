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
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableFileSystemCsv extends IndexableFileSystem {

	private char separator;
	private String lineNumberFieldName;

	public char getSeparator() {
		return separator;
	}

	public void setSeparator(char separator) {
		this.separator = separator;
	}

	public String getLineNumberFieldName() {
		return lineNumberFieldName;
	}

	public void setLineNumberFieldName(String lineNumberFieldName) {
		this.lineNumberFieldName = lineNumberFieldName;
	}

}