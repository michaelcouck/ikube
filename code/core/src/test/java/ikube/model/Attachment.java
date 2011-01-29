package ikube.model;

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

/**
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
@Entity
public class Attachment implements Serializable, Comparable<Attachment> {

	private Long attachmentId;
	private Faq faq;
	private String name;
	private Integer length;
	private byte[] attachment;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(Long id) {
		this.attachmentId = id;
	}

	@Column(length = 256)
	public String getName() {
		return name;
	}

	@Attribute(length = 256)
	public void setName(String name) {
		this.name = name;
	}

	@Lob
	@Column(length = 100000)
	@Basic(fetch = FetchType.EAGER)
	public byte[] getAttachment() {
		return attachment;
	}

	@Attribute(length = 100000)
	public void setAttachment(byte[] attachment) {
		this.attachment = attachment;
	}

	@JoinColumn(name = "faqId")
	@ManyToOne(fetch = FetchType.LAZY)
	public Faq getFaq() {
		return faq;
	}

	@Attribute()
	public void setFaq(Faq attachmentFaq) {
		this.faq = attachmentFaq;
	}

	public Integer getLength() {
		return length;
	}

	@Attribute()
	public void setLength(Integer size) {
		this.length = size;
	}

	public int compareTo(Attachment o) {
		return this.getAttachmentId() == null || o.getAttachmentId() == null ? (this.getAttachment() == null || o.getAttachment() == null ? 0
				: this.getAttachment().length < o.getAttachment().length ? 1 : 0)
				: this.getAttachmentId() < o.getAttachmentId() ? -1 : this.getAttachmentId() == o.getAttachmentId() ? 0 : 1;
	}

	public String toString() {
		return this.getClass().getSimpleName() + ":" + this.getName();
	}

}