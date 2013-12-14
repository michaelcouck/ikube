package ikube.model.geospatial;

import ikube.model.Persistable;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity
@Deprecated
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Composite<P, C> extends Persistable {

	@PrimaryKeyJoinColumn
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	private Composite<?, ?> parent;
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "parent", fetch = FetchType.EAGER)
	private List<Composite<?, ?>> children;

	public Composite<?, ?> getParent() {
		return parent;
	}

	public void setParent(final Composite<?, ?> parent) {
		this.parent = parent;
	}

	public List<Composite<?, ?>> getChildren() {
		return children;
	}

	public void setChildren(final List<Composite<?, ?>> children) {
		this.children = children;
		if (this.children != null) {
			for (Composite<?, ?> child : children) {
				child.setParent(this);
			}
		}
	}

}
