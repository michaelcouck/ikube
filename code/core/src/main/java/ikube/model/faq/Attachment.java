package ikube.model.faq;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = { @NamedQuery(name = Attachment.UPDATE_ATTACHMENT_BY_ID_GREATER_AND_SMALLER, query = Attachment.UPDATE_ATTACHMENT_BY_ID_GREATER_AND_SMALLER) })
public class Attachment implements Serializable {

	public static final String UPDATE_ATTACHMENT_BY_ID_GREATER_AND_SMALLER = "update Attachment as a set a.name = :name where a.attachmentId >= :startId and a.attachmentId < :endId";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long attachmentId;
	@JoinColumn(name = "faqId")
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	private Faq faq;
	@Column(length = 256)
	private String name;
	private int length;
	@Lob
	@Column(length = 100000)
	@Basic(fetch = FetchType.EAGER)
	private byte[] attachment;

	public long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(long id) {
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

	public int getLength() {
		return length;
	}

	public void setLength(int size) {
		this.length = size;
	}

	public String toString() {
		return this.getClass().getSimpleName() + ":" + this.getName();
	}

}