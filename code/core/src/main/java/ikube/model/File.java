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
		@NamedQuery(name = File.DELETE_ALL_FILES, query = File.DELETE_ALL_FILES),
		@NamedQuery(name = File.SELECT_FROM_FILE_BY_HASH, query = File.SELECT_FROM_FILE_BY_HASH),
		@NamedQuery(name = File.SELECT_FROM_FILE_BY_FILE_ID, query = File.SELECT_FROM_FILE_BY_FILE_ID),
		@NamedQuery(name = File.SELECT_FROM_FILE_BY_NAME, query = File.SELECT_FROM_FILE_BY_NAME),
		@NamedQuery(name = File.SELECT_COUNT_FROM_FILE_BY_NAME, query = File.SELECT_COUNT_FROM_FILE_BY_NAME),
		@NamedQuery(name = File.SELECT_FROM_FILE_BY_NAME_AND_INDEXED, query = File.SELECT_FROM_FILE_BY_NAME_AND_INDEXED),
		@NamedQuery(name = File.SELECT_FROM_FILE_WHERE_ID_GREATER_AND_NOT_INDEXED, query = File.SELECT_FROM_FILE_WHERE_ID_GREATER_AND_NOT_INDEXED) })
public class File extends Persistable {

	public static final String DELETE_ALL_FILES = "delete from File f";
	public static final String SELECT_FROM_FILE_BY_HASH = "select f from File as f where f.hash = :hash";
	public static final String SELECT_FROM_FILE_BY_FILE_ID = "select f from File as f where f.urlId = :urlId";
	public static final String SELECT_FROM_FILE_BY_NAME = "select f from File as f where f.name = :name";
	public static final String SELECT_COUNT_FROM_FILE_BY_NAME = "select count(f) from File as f where f.name = :name";
	public static final String SELECT_FROM_FILE_BY_NAME_AND_INDEXED = "select f from File as f where f.name = :name and f.indexed = :indexed";
	public static final String SELECT_FROM_FILE_WHERE_ID_GREATER_AND_NOT_INDEXED = "select f from File as f where f.id >= :id and f.indexed = :indexed";

	@Transient
	private String title;
	@Transient
	private String contentType;
	@Transient
	private byte[] rawContent;
	@Transient
	private String parsedContent;

	private long hash;
	private long urlId;
	private boolean indexed;
	private boolean temporary;
	@Column(length = 64)
	private String name;
	@Column(length = 255)
	private String url;

	public long getUrlId() {
		return urlId;
	}

	public void setUrlId(long urlId) {
		this.urlId = urlId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
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

}