package ikube.model;

import ikube.index.visitor.IndexableVisitor;

import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public abstract class Indexable<E> extends Persistable {

	public static Comparator<Indexable<?>> COMPARATOR = new Comparator<Indexable<?>>() {
		@Override
		public int compare(Indexable<?> o1, Indexable<?> o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	private String name;
	private Indexable<?> parent;
	private List<Indexable<?>> children;

	public abstract <V extends IndexableVisitor<Indexable<?>>> void accept(V visitor);

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Indexable<?> getParent() {
		return parent;
	}

	protected void setParent(Indexable<?> parent) {
		this.parent = parent;
	}

	public List<Indexable<?>> getChildren() {
		return children;
	}

	public void setChildren(final List<Indexable<?>> children) {
		this.children = children;
		if (this.children != null) {
			for (Indexable<?> child : children) {
				child.setParent(this);
			}
		}
	}

}
