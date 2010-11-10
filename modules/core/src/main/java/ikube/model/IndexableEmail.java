package ikube.model;

import ikube.index.visitor.IndexableVisitor;

import javax.persistence.Entity;

@Entity()
public class IndexableEmail extends Indexable<IndexableEmail> {

	private String idField;
	private String titleField;
	private String contentField;
	private boolean stored = Boolean.FALSE;
	private boolean analyzed = Boolean.TRUE;
	private boolean vectored = Boolean.TRUE;

	private String mailHost;
	private String username;
	private String password;
	private String port;
	private String protocol;
	private boolean secureSocketLayer;

	@Override
	public <V extends IndexableVisitor<Indexable<?>>> void accept(V visitor) {
		visitor.visit(this);
	}

	public String getIdField() {
		return idField;
	}

	public void setIdField(String idField) {
		this.idField = idField;
	}

	public String getTitleField() {
		return titleField;
	}

	public void setTitleField(String titleField) {
		this.titleField = titleField;
	}

	public String getContentField() {
		return contentField;
	}

	public void setContentField(String contentField) {
		this.contentField = contentField;
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

	public String getMailHost() {
		return mailHost;
	}

	public void setMailHost(String mailHost) {
		this.mailHost = mailHost;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public boolean isSecureSocketLayer() {
		return secureSocketLayer;
	}

	public void setSecureSocketLayer(boolean secureSocketLayer) {
		this.secureSocketLayer = secureSocketLayer;
	}

}
