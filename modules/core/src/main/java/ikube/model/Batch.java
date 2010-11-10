package ikube.model;

import javax.persistence.Entity;

@Entity()
public class Batch extends Persistable {

	private String indexName;
	private int nextRowNumber;

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public int getNextRowNumber() {
		return nextRowNumber;
	}

	public void setNextRowNumber(int nextRowNumber) {
		this.nextRowNumber = nextRowNumber;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder("[");
		builder.append(getId()).append(", ").append(getIndexName()).append(", ").append(getNextRowNumber());
		builder.append("]");
		return builder.toString();
	}

}
