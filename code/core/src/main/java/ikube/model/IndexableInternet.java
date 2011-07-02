package ikube.model;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class IndexableInternet extends Indexable<IndexableInternet> {

	private static transient final Logger LOGGER = Logger.getLogger(IndexableInternet.class);

	@Transient
	private transient String currentUrl;
	@Transient
	private transient InputStream currentInputStream;
	@Transient
	private transient Pattern pattern;

	private URI uri;
	private String url;

	@Field()
	private String titleFieldName;
	@Field()
	private String idFieldName;
	@Field()
	private String contentFieldName;

	private String excludedPattern;
	private int timeout;

	public URI getUri() {
		if (uri == null && getUrl() != null) {
			try {
				uri = new URI(getUrl());
			} catch (URISyntaxException e) {
				LOGGER.error("Exception initialising the URI : " + getUrl(), e);
			}
		}
		return uri;
	}

	public void setUri(final URI uri) {
		this.uri = uri;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getTitleFieldName() {
		return titleFieldName;
	}

	public void setTitleFieldName(final String titleFieldName) {
		this.titleFieldName = titleFieldName;
	}

	public String getIdFieldName() {
		return idFieldName;
	}

	public void setIdFieldName(final String idFieldName) {
		this.idFieldName = idFieldName;
	}

	public String getContentFieldName() {
		return contentFieldName;
	}

	public void setContentFieldName(final String contentFieldName) {
		this.contentFieldName = contentFieldName;
	}

	public String getExcludedPattern() {
		return excludedPattern;
	}

	public void setExcludedPattern(final String excludedPatterns) {
		this.excludedPattern = excludedPatterns;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	public boolean isExcluded(final String link) {
		if (pattern == null) {
			pattern = Pattern.compile(getExcludedPattern());
		}
		return pattern.matcher(link).matches();
	}

	public String getCurrentUrl() {
		return currentUrl;
	}

	public void setCurrentUrl(final String currentUrl) {
		this.currentUrl = currentUrl;
	}

	public InputStream getCurrentInputStream() {
		return currentInputStream;
	}

	public void setCurrentInputStream(final InputStream currentInputStream) {
		this.currentInputStream = currentInputStream;
	}

}
