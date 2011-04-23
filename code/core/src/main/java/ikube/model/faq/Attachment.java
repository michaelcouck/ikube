package ikube.model.faq;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
@Entity
public class Attachment implements Serializable, Comparable<Attachment> {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long attachmentId;
	@JoinColumn(name = "faqId")
	@ManyToOne(fetch = FetchType.LAZY)
	private Faq faq;
	@Column(length = 256)
	private String name;
	private Integer length;
	@Lob
	@Column(length = 100000)
	@Basic(fetch = FetchType.EAGER)
	private byte[] attachment;

	public Long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(Long id) {
		this.attachmentId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getAttachment() {
		return attachment;
	}

	public void setAttachment(byte[] attachment) {
		this.attachment = attachment;
	}

	public Faq getFaq() {
		return faq;
	}

	public void setFaq(Faq attachmentFaq) {
		this.faq = attachmentFaq;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer size) {
		this.length = size;
	}

	public int compareTo(Attachment o) {
		if (getAttachmentId() != null && o.getAttachmentId() != null) {
			return getAttachmentId().compareTo(o.getAttachmentId());
		}
		if (getName() != null && o.getName() != null) {
			return getName().compareTo(o.getName());
		}
		return 0;
	}

	public String toString() {
		return this.getClass().getSimpleName() + ":" + this.getName();
	}

	public boolean equals(Object object) {
		return EqualsBuilder.reflectionEquals(this, object);
	}

}