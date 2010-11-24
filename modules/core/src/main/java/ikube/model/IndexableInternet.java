package ikube.model;

import java.net.URI;
import java.net.URISyntaxException;

import ikube.index.visitor.IndexableVisitor;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class IndexableInternet extends Indexable<IndexableInternet> {

	private transient String currentUrl;

	private URI uri;
	private String url;
	private boolean stored = Boolean.FALSE;
	private boolean analyzed = Boolean.TRUE;
	private boolean vectored = Boolean.TRUE;

	public URI getUri() {
		if (uri == null) {
			try {
				uri = new URI(getUrl());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	@Transient
	public String getCurrentUrl() {
		return currentUrl;
	}

	public void setCurrentUrl(String currentUrl) {
		this.currentUrl = currentUrl;
	}

	@Override
	public <V extends IndexableVisitor<Indexable<?>>> void accept(V visitor) {
		visitor.visit(this);
	}

}
