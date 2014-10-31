package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableFileSystem extends Indexable {

	@Column
	@NotNull
	@Size(min = 1, max = 256 * 4)
	@Attribute(field = false, description = "This is the path to the folder where the files to be indexed are")
	private String path;
	@Column
	@Size(min = 0, max = 256 * 4)
	@Attribute(field = false, description = "This is a pattern that will be applied to the file name and path to exclude resources that are not to be indexed")
	private String excludedPattern;
	@Column
	@NotNull
	@Size(min = 0, max = 256 * 4)
	@Attribute(field = false, description = "This is a pattern that will be applied to the name and path to specifically include resources that are to be included in the index")
	private String includedPattern;
	@Column
	@Min(value = 0)
	@Max(value = 10000000)
	@Attribute(field = false, description = "This is the maximum read length that will be read from a file. This is required where files are very large and need to be read into memory completely")
	private long maxReadLength = 1000000;
	@Column
	@NotNull
	@Size(min = 1, max = 256)
	@Attribute(field = true, description = "This is the file name field in the Lucene index", name = "nameFieldName")
	private String nameFieldName;
	@Column
	@NotNull
	@Size(min = 1, max = 256)
	@Attribute(field = true, description = "This is the name of the path field in the Lucene index", name = "pathFieldName")
	private String pathFieldName;
	@Column
	@NotNull
	@Size(min = 1, max = 256)
	@Attribute(field = true, description = "This is the name of the last modified field in the Lucene index", name = "lastModifiedFieldName")
	private String lastModifiedFieldName;
	@Column
	@NotNull
	@Size(min = 1, max = 256)
	@Attribute(field = true, description = "This is the name of the content field in the Lucene index", name = "contentFieldName")
	private String contentFieldName;
	@Column
	@NotNull
	@Size(min = 1, max = 256)
	@Attribute(field = true, description = "This is the name of the length field in the Lucene index", name = "lengthFieldName")
	private String lengthFieldName;
	@Column
	@Min(value = 1)
	@Max(value = 100000)
	@Attribute(description = "This is the name of the batch size for files, i.e. how many files each thread will batch, not read in one shot, typical would be 1000")
	private int batchSize;
	@Column
	@Attribute(description = "Whether to unpack the zip files found, this is deprecated and done automatically by reading in the zips and jars")
	private boolean unpackZips = Boolean.TRUE;

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

	public long getMaxReadLength() {
		return maxReadLength;
	}

	public void setMaxReadLength(long maxReadLength) {
		this.maxReadLength = maxReadLength;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public boolean isUnpackZips() {
		return unpackZips;
	}

	public void setUnpackZips(boolean unpackZips) {
		this.unpackZips = unpackZips;
	}

}
