package ikube.model;

import org.apache.openjpa.persistence.jdbc.Index;

import javax.persistence.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-11-2010
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = {@NamedQuery(name = Url.DELETE_ALL_URLS, query = Url.DELETE_ALL_URLS),
        @NamedQuery(name = Url.SELECT_FROM_URL_BY_HASH, query = Url.SELECT_FROM_URL_BY_HASH),
        @NamedQuery(name = Url.SELECT_FROM_URL_BY_NAME, query = Url.SELECT_FROM_URL_BY_NAME)})
public class Url extends Persistable {

    public static final String DELETE_ALL_URLS = "delete from Url u";
    public static final String SELECT_FROM_URL_BY_HASH = "select u from Url as u where u.hash = :hash";
    public static final String SELECT_FROM_URL_BY_NAME = "select u from Url as u where u.name = :name";

    @Transient
    private String title;
    @Transient
    private String contentType;
    @Transient
    private String parsedContent;

    @Column
    private boolean indexed;
    @Column
    @Index(name = "hash_index", enabled = true)
    private long hash;
    @Column(length = 1024)
    @Index(name = "name_index", enabled = true)
    private String name;
    @Column(length = 4096)
    private String url;
    @Lob
    @Column
    private byte[] rawContent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public byte[] getRawContent() {
        return rawContent;
    }

    public void setRawContent(final byte[] rawContent) {
        this.rawContent = rawContent;
    }

    public String getParsedContent() {
        return parsedContent;
    }

    public void setParsedContent(final String parsedContent) {
        this.parsedContent = parsedContent;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(final boolean indexed) {
        this.indexed = indexed;
    }

    public long getHash() {
        return hash;
    }

    public void setHash(final long hash) {
        this.hash = hash;
    }

    public String toString() {
        return getUrl();
    }

}