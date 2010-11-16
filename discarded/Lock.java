package ikube.model;

import javax.persistence.Entity;

@Entity()
public class Lock extends Persistable {

	private String className;
	private boolean locked;
	private long start;

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(final boolean locked) {
		this.locked = locked;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	public long getStart() {
		return start;
	}

	public void setStart(final long start) {
		this.start = start;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder("[");
		builder.append(getId()).append(", ").append(isLocked()).append(", ").append(getClassName());
		builder.append("]");
		return builder.toString();
	}

}
