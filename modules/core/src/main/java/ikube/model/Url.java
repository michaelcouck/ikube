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
	private String name;
	private Boolean indexed;
	private Long hash;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String indexName) {
		this.name = indexName;
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
		builder.append(getName());
		builder.append(", ");
		builder.append(getUrl());
		builder.append("]");
		return builder.toString();
	}

}