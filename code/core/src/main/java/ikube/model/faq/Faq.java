package ikube.model.faq;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Faq implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long faqId;
	@Column(length = 1024)
	private String question;
	@Column(length = 1024)
	private String answer;
	@Column(length = 64)
	private String creator;
	@Column(length = 64)
	private String modifier;
	@Column(nullable = false)
	private boolean published;
	private Timestamp creationTimestamp;
	private Timestamp modifiedTimestamp;
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "faq", fetch = FetchType.EAGER)
	private Collection<Attachment> attachments;

	public long getFaqId() {
		return faqId;
	}

	public void setFaqId(long faqId) {
		this.faqId = faqId;
	}

	public Collection<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(Collection<Attachment> attachments) {
		this.attachments = attachments;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String name) {
		this.question = name;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public Timestamp getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(Timestamp creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public Timestamp getModifiedTimestamp() {
		return modifiedTimestamp;
	}

	public void setModifiedTimestamp(Timestamp modifiedTimestamp) {
		this.modifiedTimestamp = modifiedTimestamp;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String text) {
		this.answer = text;
	}

	public boolean getPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}

}