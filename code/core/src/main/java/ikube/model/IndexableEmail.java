package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableEmail extends Indexable<IndexableEmail> {

	@Column
	@NotNull
	@Size(min = 1, max = 256)
	@Attribute(field = false, description = "This is the name of the id field in the Lucene index")
	private String idField;
	@Column
	@NotNull
	@Size(min = 1, max = 256)
	@Attribute(field = false, description = "This is the name of the title field in the Lucene index")
	private String titleField;
	@Column
	@NotNull
	@Size(min = 1, max = 256)
	@Attribute(field = false, description = "This is the namd of the content field in the Lucene index")
	private String contentField;

	@Column
	@NotNull
	@Size(min = 1, max = 256)
	@Attribute(field = false, description = "The url where the mail is hosted, i.e. the Imap or Pop server")
	private String mailHost;
	@Column
	@NotNull
	@Size(min = 1, max = 256)
	@Attribute(field = false, description = "The user name of the mail account")
	private String username;
	@Column
	@NotNull
	@Size(min = 1, max = 256)
	@Attribute(field = false, description = "The password for the mail account")
	private String password;
	@Column
	@NotNull
	@Size(min = 1, max = 5)
	@Attribute(field = false, description = "The port number of the mail account")
	private String port;
	@Column
	@NotNull
	@Size(min = 1, max = 5)
	@Attribute(field = false, description = "The protocol of the account, Imap or Pop3 for example")
	private String protocol;
	@Column
	@Attribute(field = false, description = "Whether to use SSL for the mail access")
	private boolean secureSocketLayer;

	public String getIdField() {
		return idField;
	}

	public void setIdField(final String idField) {
		this.idField = idField;
	}

	public String getTitleField() {
		return titleField;
	}

	public void setTitleField(final String titleField) {
		this.titleField = titleField;
	}

	public String getContentField() {
		return contentField;
	}

	public void setContentField(final String contentField) {
		this.contentField = contentField;
	}

	public String getMailHost() {
		return mailHost;
	}

	public void setMailHost(final String mailHost) {
		this.mailHost = mailHost;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getPort() {
		return port;
	}

	public void setPort(final String port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(final String protocol) {
		this.protocol = protocol;
	}

	public boolean isSecureSocketLayer() {
		return secureSocketLayer;
	}

	public void setSecureSocketLayer(final boolean secureSocketLayer) {
		this.secureSocketLayer = secureSocketLayer;
	}

}
