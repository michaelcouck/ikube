package ikube.model;

import ikube.action.index.handler.IStrategy;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
@Entity
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Indexable extends Persistable {

    @Transient
    private String type = this.getClass().getName();

    @Transient
    @Attribute(field = false, description = "This is the content of the indexable, it is therefore only valid " +
            "while indexing and for the current resource.")
    private transient volatile Object content;

    @Transient
    @Attribute(field = false, description = "Similar to the above, this is the raw unprocessed content, potentially binary data, " +
            "numbers and characters etc.")
    private transient volatile Object rawContent;

    @Transient
    @Attribute(field = false, description = "This is the address constructed content, the handler will construct and use this if necessary.")
    private transient volatile String addressContent;
    
    @Transient
    @Attribute(field = false, description = "The holder for the maximum number of exceptions that this handler will allow before terminating.")
    private AtomicInteger exceptions = new AtomicInteger(0);

    @Transient
    @Attribute(field = false, description = "These strategies will be processed before processing the indexable.")
    private transient List<IStrategy> strategies;

    @Column
    @Attribute(field = false, description = "The name of this indexable")
    private String name;
    @Column
    @Attribute(field = false, description = "Whether this is a geospatial address field")
    private boolean address = Boolean.FALSE;
    @Column
    @Attribute(field = false, description = "Whether this value should be stored in the index")
    private boolean stored = Boolean.FALSE;
    @Column
    @Attribute(field = false, description = "Whether this field should be analyzed for stemming and so on")
    private boolean analyzed = Boolean.TRUE;
    @Column
    @Attribute(field = false, description = "Whether this field should be vectored in the index")
    private boolean vectored = Boolean.FALSE;
    @Column
    @Attribute(field = false, description = "Whether this field should have the normalization omitted, " +
            "i.e. the tf-idf omitted, meaning that longer documents will score higher if the norms are omitted(i.e. " +
            "they are not written to the index)")
    private boolean omitNorms = Boolean.FALSE;
    @Column
    @Attribute(field = false, description = "Whether this field should have the terms tokenized")
    private boolean tokenized = Boolean.TRUE;
    @Column
    @Attribute(field = false, description = "The boost to give the field at index time")
    private float boost;
    @Column
    @Min(value = 1)
    @Max(value = 1000000)
    @Attribute(field = false, description = "This is the maximum exceptions during indexing before the indexing is " +
            "stopped")
    private long maxExceptions = 1000;
    @Column
    @Min(value = 1)
    @Max(value = 1000)
    @Attribute(field = false, description = "This is the number of threads that should be spawned for this indexable")
    private int threads = 1;

    // Note to self: You cannot have a primary key join with bi-directional mapping!!!
    // @PrimaryKeyJoinColumn
    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private Indexable parent;
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "parent", fetch = FetchType.EAGER)
    private List<Indexable> children;

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Indexable getParent() {
        return parent;
    }

    public void setParent(final Indexable parent) {
        this.parent = parent;
    }

    public List<Indexable> getChildren() {
        return children;
    }

    public void setChildren(final List<Indexable> children) {
        this.children = children;
        if (this.children != null) {
            for (Indexable child : children) {
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

    public boolean isOmitNorms() {
        return omitNorms;
    }

    public void setTokenized(final boolean tokenized) {
        this.tokenized = tokenized;
    }

    public boolean isTokenized() {
        return tokenized;
    }

    public float getBoost() {
        return boost;
    }

    public void setBoost(float boost) {
        this.boost = boost;
    }

    public void setOmitNorms(boolean omitNorms) {
        this.omitNorms = omitNorms;
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

    public Object getRawContent() {
        return rawContent;
    }

    public void setRawContent(Object rawContent) {
        this.rawContent = rawContent;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}