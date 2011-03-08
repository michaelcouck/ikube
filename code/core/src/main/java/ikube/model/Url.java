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
	private boolean indexed;
	/** The hash of the content. */
	private long hash;

	private transient String contentType;
	private transient String title;
	private transient byte[] rawContent;
	private transient String parsedContent;

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
		return rawContent;
	}

	public void setRawContent(final byte[] rawContent) {
		this.rawContent = rawContent;
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
		builder.append(", ");
		builder.append(getTitle());
		builder.append(", ");
		builder.append(getRawContent());
		builder.append(", ");
		builder.append(getParsedContent());
		builder.append("]");
		return builder.toString();
	}

}