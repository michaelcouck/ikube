package ikube.model.medical;

import ikube.model.Persistable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;

/**
 * This class represents any notes or attachments that were added to the patient's medical records.
 * 
 * @author Michael Couck
 * @since 05.03.2011
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Record extends Persistable {

	@Lob
	@Column(length = 10000)
	@Basic(fetch = FetchType.EAGER)
	private String record;

	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = record;
	}

}
