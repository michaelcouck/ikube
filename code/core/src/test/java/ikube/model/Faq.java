package ikube.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

/**
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
@Entity
@DiscriminatorValue("Faq")
@Inheritance(strategy = InheritanceType.JOINED)
public class Faq implements Serializable, Comparable<Faq> {

	private Long faqId;
	private String question;
	private String answer;
	private String creator;
	private String modifier;
	private Boolean published;
	private Timestamp creationTimestamp;
	private Timestamp modifiedTimestamp;
	private Set<Attachment> attachments = new TreeSet<Attachment>();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getFaqId() {
		return faqId;
	}

	public void setFaqId(Long faqId) {
		this.faqId = faqId;
	}

	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "faq", fetch = FetchType.LAZY)
	public Set<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(Set<Attachment> attachments) {
		this.attachments = attachments;
	}

	@Column(length = 1024)
	public String getQuestion() {
		return question;
	}

	@Attribute(length = 1024)
	public void setQuestion(String name) {
		this.question = name;
	}

	@Column(length = 64)
	public String getCreator() {
		return creator;
	}

	@Attribute(length = 64)
	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Column(length = 64)
	public String getModifier() {
		return modifier;
	}

	@Attribute(length = 64)
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public Timestamp getCreationTimestamp() {
		return creationTimestamp;
	}

	@Attribute()
	public void setCreationTimestamp(Timestamp creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public Timestamp getModifiedTimestamp() {
		return modifiedTimestamp;
	}

	@Attribute()
	public void setModifiedTimestamp(Timestamp modifiedTimestamp) {
		this.modifiedTimestamp = modifiedTimestamp;
	}

	@Column(length = 1024)
	public String getAnswer() {
		return answer;
	}

	@Attribute(length = 1024)
	public void setAnswer(String text) {
		this.answer = text;
	}

	@Column(nullable = false)
	public Boolean getPublished() {
		return published;
	}

	@Attribute(length = 1024)
	public void setPublished(Boolean published) {
		this.published = published;
	}

	public String toString() {
		return this.getClass().getSimpleName() + ":" + this.getFaqId();
	}

	@Override
	public int compareTo(Faq o) {
		if (this.getFaqId() == null || o.getFaqId() == null) {
			return 0;
		}
		return this.getFaqId() < o.getFaqId() ? -1 : this.getFaqId() == o.getFaqId() ? 0 : 1;
	}

}