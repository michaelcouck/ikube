package ikube.model;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author Michael Couck
 * @since 23.11.10
 * @version 01.00
 */
@Entity()
@Table(name = "file_")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = { 
		@NamedQuery(name = File.SELECT_FROM_FILE_BY_NAME, query = File.SELECT_FROM_FILE_BY_NAME),
		@NamedQuery(name = File.SELECT_FROM_FILE_BY_INDEX_NAME, query = File.SELECT_FROM_FILE_BY_INDEX_NAME),
		@NamedQuery(name = File.SELECT_FROM_FILE_BY_INDEX_NAME_AND_PATH, query = File.SELECT_FROM_FILE_BY_INDEX_NAME_AND_PATH) })
public class File extends Persistable {

	public static final String SELECT_FROM_FILE_BY_NAME = "select f from File as f where f.name = :name";
	public static final String SELECT_FROM_FILE_BY_INDEX_NAME = "select f from File as f where f.indexName = :indexName";
	public static final String SELECT_FROM_FILE_BY_INDEX_NAME_AND_PATH = "select f from File as f where f.indexName = :indexName and f.path = :path";

	@Transient
	private String title;
	@Transient
	private String contentType;
	@Transient
	private byte[] rawContent;
	@Transient
	private String parsedContent;

	@Column
	private long hash;
	@Column
	private long pathId;
	@Column
	private boolean indexed;
	@Column
	private boolean temporary;
	@Column(length = 64)
	private String name;
	@Column(length = 32)
	private String indexName;
	@Column(length = 255)
	private String path;
	@Column
	private long length;
	@Column
	private long lastModified;

	public long getPathId() {
		return pathId;
	}

	public void setPathId(long urlId) {
		this.pathId = urlId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexContextName) {
		this.indexName = indexContextName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String url) {
		this.path = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	public byte[] getRawContent() {
		if (rawContent == null) {
			return null;
		}
		return Arrays.copyOf(rawContent, rawContent.length);
	}

	public void setRawContent(final byte[] rawContent) {
		if (rawContent == null) {
			this.rawContent = null;
			return;
		}
		this.rawContent = Arrays.copyOf(rawContent, rawContent.length);
	}

	public String getParsedContent() {
		return parsedContent;
	}

	public void setParsedContent(final String parsedContent) {
		this.parsedContent = parsedContent;
	}

	public boolean isIndexed() {
		return indexed;
	}

	public void setIndexed(final boolean indexed) {
		this.indexed = indexed;
	}

	public boolean isTemporary() {
		return temporary;
	}

	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}

	public long getHash() {
		return hash;
	}

	public void setHash(final long hash) {
		this.hash = hash;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

}