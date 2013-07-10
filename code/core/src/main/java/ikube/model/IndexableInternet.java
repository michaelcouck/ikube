package ikube.model;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableInternet extends Indexable<IndexableInternet> {

	@Transient
	private transient String currentUrl;
	@Transient
	private transient InputStream currentInputStream;
	@Transient
	private transient java.util.regex.Pattern pattern;
	@Transient
	private transient URI uri;
	@Transient
	private transient String baseUrl;

	@Column
	@NotNull
	@Pattern(regexp = "^(https?|http?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", message = "The url must be valid")
	@Attribute(field = false, description = "This is the primary url that will be crawled")
	private String url;
	@Column
	@Pattern(regexp = "^(https?|http?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", message = "The url must be valid")
	@Attribute(field = false, description = "This is the url to the login page if it is a protected site")
	private String loginUrl;
	@Column
	@Size(min = 1, max = 256)
	@Attribute(field = false, description = "This is the userid to login to the site")
	private String userid;
	@Column
	@Size(min = 1, max = 256)
	@Attribute(field = false, description = "This is the password to login to the site")
	private String password;
	@Column
	@Min(value = 1)
	@Max(value = 100000)
	@Attribute(field = false, description = "This is the size that the batches of urls will be per thread")
	private int internetBatchSize;
	@Column
	@Attribute(field = false, description = "This is is a pattern that will be appled to exclude any urls, i.e. urls that should not be crawled, like confidential pages etc.")
	private String excludedPattern;
	@Column
	@Min(value = 1)
	@Max(value = 60000)
	@Attribute(field = false, description = "This is the length of time that the crawler will wait for a particular page to be delivered")
	private int timeout;
	@Column
	@Size(min = 1, max = 256)
	@Attribute(field = false, description = "This is the name of the title field in the Lucene index")
	private String titleFieldName;
	@Column
	@Size(min = 1, max = 256)
	@Attribute(field = false, description = "This is the name of the id field in the Lucene index")
	private String idFieldName;
	@Column
	@Size(min = 1, max = 256)
	@Attribute(field = false, description = "This is the name of the content field int he Lucene index")
	private String contentFieldName;
	@Column
	@Attribute(field = false, description = "This is the maximum length of bytes that will be read from the input stream")
	private long maxReadLength;

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

	public void setUri(final URI uri) {
		this.uri = uri;
	}

	public String getUrl() {
		return url;
	}

	public String getBaseUrl() {
		if (baseUrl == null) {
			int lastDotIndex = getUri().getPath().lastIndexOf('.');
			if (lastDotIndex < 0) {
				baseUrl = getUrl();
			} else {
				lastDotIndex = getUri().toString().lastIndexOf('/');
				baseUrl = getUri().toString().substring(0, lastDotIndex);
			}
		}
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public long getMaxReadLength() {
		return maxReadLength;
	}

	public void setMaxReadLength(long maxReadLength) {
		this.maxReadLength = maxReadLength;
	}

	public int getInternetBatchSize() {
		return internetBatchSize;
	}

	public void setInternetBatchSize(int interrnetBatchSize) {
		this.internetBatchSize = interrnetBatchSize;
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

	public boolean isExcluded(final String string) {
		if (pattern == null) {
			if (getExcludedPattern() != null) {
				pattern = java.util.regex.Pattern.compile(getExcludedPattern());
			} else {
				return Boolean.FALSE;
			}
		}
		return pattern.matcher(string).matches();
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
