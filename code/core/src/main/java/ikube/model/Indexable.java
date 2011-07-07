package ikube.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Indexable<E> extends Persistable {

	private String name;
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	private Indexable<?> parent;
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "parent", fetch = FetchType.EAGER)
	private List<Indexable<?>> children;
	private boolean address;

	private boolean stored = Boolean.FALSE;
	private boolean analyzed = Boolean.TRUE;
	private boolean vectored = Boolean.FALSE;

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Indexable<?> getParent() {
		return parent;
	}

	public void setParent(final Indexable<?> parent) {
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

	public boolean isAddress() {
		return address;
	}

	public void setAddress(final boolean address) {
		this.address = address;
	}

	public boolean isStored() {
		return stored;
	}

	public void setStored(final boolean stored) {
		this.stored = stored;
	}

	public boolean isAnalyzed() {
		return analyzed;
	}

	public void setAnalyzed(final boolean analyzed) {
		this.analyzed = analyzed;
	}

	public boolean isVectored() {
		return vectored;
	}

	public void setVectored(final boolean vectored) {
		this.vectored = vectored;
	}

}
