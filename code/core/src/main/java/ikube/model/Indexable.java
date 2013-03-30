package ikube.model;

import ikube.action.index.handler.IStrategy;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.JOINED)
public class Indexable<E> extends Persistable {

	/** These strategies will be processed before processing the indexable. */
	@Transient
	private transient List<IStrategy> strategies;

	@Column
	@Attribute(field = false, description = "The name of this indexable")
	private String name;
	@PrimaryKeyJoinColumn
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	private Indexable<?> parent;
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "parent", fetch = FetchType.EAGER)
	private List<Indexable<?>> children;
	@Column
	@Attribute(field = false, description = "Whether this is a geospatial address field")
	private boolean address = Boolean.FALSE;

	@Column
	@Attribute(field = false, description = "Whether this value should be stored in the index")
	private boolean stored = Boolean.TRUE;
	@Column
	@Attribute(field = false, description = "Whether this field should be analyzed for stemming and so on")
	private boolean analyzed = Boolean.TRUE;
	@Column
	@Attribute(field = false, description = "Whether this field should be vectored in the index")
	private boolean vectored = Boolean.TRUE;

	@Column
	@Min(value = 1)
	@Max(value = 1000000)
	@Attribute(field = false, description = "This is the maximum exceptions during indexing before the indexing is stopped")
	private long maxExceptions = 1000;

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

	public long getMaxExceptions() {
		return maxExceptions;
	}

	public void setMaxExceptions(long maxExceptions) {
		this.maxExceptions = maxExceptions;
	}

	public List<IStrategy> getStrategies() {
		return strategies;
	}

	public void setStrategies(List<IStrategy> strategies) {
		this.strategies = strategies;
	}

}