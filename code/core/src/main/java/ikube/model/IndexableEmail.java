package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class IndexableEmail extends Indexable<IndexableEmail> {

	@Field()
	private String idField;
	@Field()
	private String titleField;
	@Field()
	private String contentField;

	private String mailHost;
	private String username;
	private String password;
	private String port;
	private String protocol;
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
