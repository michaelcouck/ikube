package ikube.model;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import org.apache.openjpa.persistence.jdbc.Index;

/**
 * @author Michael Couck
 * @since 23.11.10
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = {
		@NamedQuery(name = Url.DELETE_ALL_URLS, query = Url.DELETE_ALL_URLS),
		@NamedQuery(name = Url.SELECT_FROM_URL_BY_HASH, query = Url.SELECT_FROM_URL_BY_HASH),
		@NamedQuery(name = Url.SELECT_FROM_URL_BY_URL_ID, query = Url.SELECT_FROM_URL_BY_URL_ID),
		@NamedQuery(name = Url.SELECT_FROM_URL_BY_NAME, query = Url.SELECT_FROM_URL_BY_NAME),
		@NamedQuery(name = Url.SELECT_COUNT_FROM_URL_BY_NAME, query = Url.SELECT_COUNT_FROM_URL_BY_NAME),
		@NamedQuery(name = Url.SELECT_FROM_URL_BY_NAME_AND_INDEXED, query = Url.SELECT_FROM_URL_BY_NAME_AND_INDEXED),
		@NamedQuery(name = Url.SELECT_FROM_URL_WHERE_ID_GREATER_AND_NOT_INDEXED, query = Url.SELECT_FROM_URL_WHERE_ID_GREATER_AND_NOT_INDEXED) })
public class Url extends Persistable {

	public static final String DELETE_ALL_URLS = "delete from Url u";
	public static final String SELECT_FROM_URL_BY_HASH = "select u from Url as u where u.hash = :hash";
	public static final String SELECT_FROM_URL_BY_URL_ID = "select u from Url as u where u.urlId = :urlId";
	public static final String SELECT_FROM_URL_BY_NAME = "select u from Url as u where u.name = :name";
	public static final String SELECT_COUNT_FROM_URL_BY_NAME = "select count(u) from Url as u where u.name = :name";
	public static final String SELECT_FROM_URL_BY_NAME_AND_INDEXED = "select u from Url as u where u.name = :name and u.indexed = :indexed";
	public static final String SELECT_FROM_URL_WHERE_ID_GREATER_AND_NOT_INDEXED = "select u from Url as u where u.id >= :id and u.indexed = :indexed";

	@Transient
	private String contentType;
	@Transient
	private String title;
	@Transient
	private byte[] rawContent;
	@Transient
	private String parsedContent;

	@Index(name = "urlId_index", enabled = true)
	private long urlId;
	private boolean indexed;
	@Index(name = "hash_index", enabled = true)
	private long hash;
	@Column(length = 64)
	@Index(name = "name_index", enabled = true)
	private String name;
	@Column(length = 512)
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

	public long getHash() {
		return hash;
	}

	public void setHash(final long hash) {
		this.hash = hash;
	}

}