package ikube.model;

import ikube.index.visitor.IndexableVisitor;

import java.util.List;

import javax.persistence.Entity;

@Entity()
public abstract class Indexable<E> extends Persistable {

	private String name;
	private List<Indexable<?>> children;

	public abstract <V extends IndexableVisitor<Indexable<?>>> void accept(V visitor);

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<Indexable<?>> getChildren() {
		return children;
	}

	public void setChildren(final List<Indexable<?>> children) {
		this.children = children;
	}

}
