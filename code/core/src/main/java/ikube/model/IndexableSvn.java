package ikube.model;

import org.tmatesoft.svn.core.io.SVNRepository;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 04-06-2014
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableSvn extends Indexable {

	@Transient
	private transient SVNRepository repository;

	/**
	 * Login details for the repository.
	 */
	@Column
	@NotNull
	@Attribute(field = false, description = "The user name for the login to the repository")
	private String username;
	@Column
	@NotNull
	@Attribute(field = false, description = "The password for the login to the repository")
	private String password;
	@Column
	@NotNull
	@Attribute(field = false, description = "The url to the svn repository")
	private String url;
	@Column
	@NotNull
	@Attribute(field = false, description = "The initial file path in the repository")
	private String filePath;

	@Column
	@NotNull
	@Attribute(field = false, description = "The relative file path of the resource in the repository")
	private String relativeFilePath;

	@Column
	@NotNull
	@Attribute(field = true, description = "The author of the revision")
	private String author;

	@Column
	@NotNull
	@Attribute(field = true, description = "The commit comment of the revision by the author")
	private String commitComment;

	@Column
	@NotNull
	@Attribute(field = true, description = "The date of commit of the revision")
	private String revisionDate;

	@Column
	@NotNull
	@Attribute(field = true, description = "The name of the resource, eg. SomeJavaFile.java")
	private String resourceName;

	@Column
	@NotNull
	@Attribute(field = true, description = "The number of the revision")
	private String revision;

	@Column
	@NotNull
	@Attribute(field = true, description = "The size of the resource")
	private String size;

	public SVNRepository getRepository() {
		return repository;
	}

	public void setRepository(SVNRepository repository) {
		this.repository = repository;
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getRelativeFilePath() {
		return relativeFilePath;
	}

	public void setRelativeFilePath(String relativeFilePath) {
		this.relativeFilePath = relativeFilePath;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCommitComment() {
		return commitComment;
	}

	public void setCommitComment(String commitComment) {
		this.commitComment = commitComment;
	}

	public String getRevisionDate() {
		return revisionDate;
	}

	public void setRevisionDate(String revisionDate) {
		this.revisionDate = revisionDate;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
}
