package ikube.model;

import javax.persistence.Entity;

/**
 * @author Michael Couck
 * @since 23.11.10
 * @version 01.00
 */
@Entity()
public class Url extends Persistable {

	private String url;
	private String contentType;
	private Boolean indexed;
	/** The hash of the content. */
	private Long hash;

	private transient String title;
	private transient byte[] rawContent;
	private transient String parsedContent;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getRawContent() {
		return rawContent;
	}

	public void setRawContent(byte[] rawContent) {
		this.rawContent = rawContent;
	}

	public String getParsedContent() {
		return parsedContent;
	}

	public void setParsedContent(String parsedContent) {
		this.parsedContent = parsedContent;
	}

	public Boolean isIndexed() {
		return indexed;
	}

	public void setIndexed(Boolean indexed) {
		this.indexed = indexed;
	}

	public Long getHash() {
		return hash;
	}

	public void setHash(Long hash) {
		this.hash = hash;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(getId());
		builder.append(", ");
		builder.append(isIndexed());
		builder.append(", ");
		builder.append(getUrl());
		builder.append(", ");
		builder.append(getHash());
		builder.append("]");
		return builder.toString();
	}

}