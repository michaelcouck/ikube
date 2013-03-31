package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableFileSystemCsv extends IndexableFileSystem {

	@Transient
	private transient java.io.File file;
	@Transient
	private transient int lineNumber;

	private String separator;
	private String encoding;
	private String lineNumberFieldName;

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(final String separator) {
		this.separator = separator;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getLineNumberFieldName() {
		return lineNumberFieldName;
	}

	public void setLineNumberFieldName(String lineNumberFieldName) {
		this.lineNumberFieldName = lineNumberFieldName;
	}

	public java.io.File getFile() {
		return file;
	}

	public void setFile(java.io.File file) {
		this.file = file;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

}