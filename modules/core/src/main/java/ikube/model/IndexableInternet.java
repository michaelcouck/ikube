package ikube.model;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class IndexableInternet extends Indexable<IndexableInternet> {

	@Transient
	private transient String currentUrl;
	@Transient
	private transient InputStream currentInputStream;

	private URI uri;
	private String url;

	public URI getUri() {
		if (uri == null && getUrl() != null) {
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

	@Transient
	public String getCurrentUrl() {
		return currentUrl;
	}

	public void setCurrentUrl(String currentUrl) {
		this.currentUrl = currentUrl;
	}

	@Transient
	public InputStream getCurrentInputStream() {
		return currentInputStream;
	}

	public void setCurrentInputStream(InputStream currentInputStream) {
		this.currentInputStream = currentInputStream;
	}

}
