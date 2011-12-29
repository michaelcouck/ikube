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
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class IndexableFileSystem extends Indexable<IndexableFileSystem> {

	private String path;
	private String excludedPattern;
	private String includedPattern;
	private long maxReadLength = 10000000;

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
	@Field()
	private int batchSize;
	private boolean unpackZips;

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
