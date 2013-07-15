package ikube.model;

import ikube.action.index.handler.IStrategy;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
@Entity
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Indexable<E> extends Persistable {

	/** This is the content of the indexable, it is therefore only valid while indexing and for the current resource. */
	@Transient
	private transient volatile Object content;
	@Transient
	private transient volatile String addressContent;
	@Transient
	private AtomicInteger exceptions = new AtomicInteger(0);
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

	@Column
	@Min(value = 1)
	@Max(value = 1000)
	@Attribute(field = false, description = "This is the number of threads that should be spawned for this indexable")
	private int threads = 1;

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

	public void setMaxExceptions(final long maxExceptions) {
		this.maxExceptions = maxExceptions;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(final int threads) {
		this.threads = threads;
	}

	public int incrementThreads(final int increment) {
		threads += increment;
		return threads;
	}

	public List<IStrategy> getStrategies() {
		return strategies;
	}

	public void setStrategies(final List<IStrategy> strategies) {
		this.strategies = strategies;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(final Object content) {
		this.content = content;
	}

	public String getAddressContent() {
		return addressContent;
	}

	public void setAddressContent(String addressContent) {
		this.addressContent = addressContent;
	}

	public int getExceptions() {
		return exceptions.get();
	}

	public void setExceptions(int exceptions) {
		this.exceptions.set(exceptions);
	}

	public int incrementAndGetExceptions() {
		return exceptions.incrementAndGet();
	}

}