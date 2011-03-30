package ikube.model;

import java.io.File;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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
	private String excludedPattern;
	private String includedPattern;

	@Field()
	private String nameFieldName;
	@Field()
	private String pathFieldName;
	@Field()
	private String lastModifiedFieldName;
	@Field()
	private String contentFieldName;
	@Field()
	private String lengthFieldName;

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public String getNameFieldName() {
		return nameFieldName;
	}

	public void setNameFieldName(final String nameFieldName) {
		this.nameFieldName = nameFieldName;
	}

	public String getPathFieldName() {
		return pathFieldName;
	}

	public void setPathFieldName(final String pathFieldName) {
		this.pathFieldName = pathFieldName;
	}

	public String getLastModifiedFieldName() {
		return lastModifiedFieldName;
	}

	public void setLastModifiedFieldName(final String lastModifiedFieldName) {
		this.lastModifiedFieldName = lastModifiedFieldName;
	}

	public String getLengthFieldName() {
		return lengthFieldName;
	}

	public void setLengthFieldName(final String lengthFieldName) {
		this.lengthFieldName = lengthFieldName;
	}

	public String getContentFieldName() {
		return contentFieldName;
	}

	public void setContentFieldName(final String contentFieldName) {
		this.contentFieldName = contentFieldName;
	}

	public String getExcludedPattern() {
		return excludedPattern;
	}

	public void setExcludedPattern(final String excludedPattern) {
		this.excludedPattern = excludedPattern;
	}

	public String getIncludedPattern() {
		return includedPattern;
	}

	public void setIncludedPattern(final String includedPattern) {
		this.includedPattern = includedPattern;
	}

	@Transient
	public File getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(final File currentFile) {
		this.currentFile = currentFile;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
