package ikube.model;

import java.io.File;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class IndexableFileSystem extends Indexable<IndexableFileSystem> {

	@Transient
	private transient File currentFile;

	private String path;
	private String nameFieldName;
	private String pathFieldName;
	private String lastModifiedFieldName;
	private String lengthFieldName;
	private String contentFieldName;
	private String excludedPattern;
	private String includedPattern;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getNameFieldName() {
		return nameFieldName;
	}

	public void setNameFieldName(String nameFieldName) {
		this.nameFieldName = nameFieldName;
	}

	public String getPathFieldName() {
		return pathFieldName;
	}

	public void setPathFieldName(String pathFieldName) {
		this.pathFieldName = pathFieldName;
	}

	public String getLastModifiedFieldName() {
		return lastModifiedFieldName;
	}

	public void setLastModifiedFieldName(String lastModifiedFieldName) {
		this.lastModifiedFieldName = lastModifiedFieldName;
	}

	public String getLengthFieldName() {
		return lengthFieldName;
	}

	public void setLengthFieldName(String lengthFieldName) {
		this.lengthFieldName = lengthFieldName;
	}

	public String getContentFieldName() {
		return contentFieldName;
	}

	public void setContentFieldName(String contentFieldName) {
		this.contentFieldName = contentFieldName;
	}

	public String getExcludedPattern() {
		return excludedPattern;
	}

	public void setExcludedPattern(String excludedPattern) {
		this.excludedPattern = excludedPattern;
	}

	public String getIncludedPattern() {
		return includedPattern;
	}

	public void setIncludedPattern(String includedPattern) {
		this.includedPattern = includedPattern;
	}

	@Transient
	public File getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(File currentFile) {
		this.currentFile = currentFile;
	}

}
