package ikube.model.geospatial;

import ikube.model.Persistable;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;

public class Composite<P, C> extends Persistable {

	@PrimaryKeyJoinColumn
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	private Composite<P, C> parent;
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "parent", fetch = FetchType.EAGER)
	private List<Composite<P, C>> children;

	public Composite<P, C> getParent() {
		return parent;
	}

	public void setParent(final Composite<P, C> parent) {
		this.parent = parent;
	}

	public List<Composite<P, C>> getChildren() {
		return children;
	}

	public void setChildren(final List<Composite<P, C>> children) {
		this.children = children;
		if (this.children != null) {
			for (Composite<P, C> child : children) {
				child.setParent(this);
			}
		}
	}

}
