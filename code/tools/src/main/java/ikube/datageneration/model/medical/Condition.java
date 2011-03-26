package ikube.datageneration.model.medical;

import ikube.datageneration.model.Persistable;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;

/**
 * This class represents a medical condition, could be a disease or a symptom for example.
 * 
 * @author Michael Couck
 * @since 05.03.2011
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Condition extends Persistable {

	@Column(length = 64)
	private String name;
	@Column(length = 256)
	private String description;

	@ManyToMany(cascade = CascadeType.REFRESH, mappedBy = "conditions", fetch = FetchType.LAZY, targetEntity = Medication.class)
	private Collection<Medication> medications;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Collection<Medication> getMedications() {
		return medications;
	}

	public void setMedications(Collection<Medication> medications) {
		this.medications = medications;
	}

}
