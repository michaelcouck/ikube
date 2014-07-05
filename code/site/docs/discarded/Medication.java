package ikube.model.medical;

import ikube.model.Persistable;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;

/**
 * This class represents a medication or drug.
 * 
 * @author Michael Couck
 * @since 05.03.2011
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Medication extends Persistable {

	@Column(length = 64)
	private String name;
	@Column(length = 64)
	private String brand;
	@Column(length = 32)
	private int strength;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = Condition.class)
	private Collection<Condition> conditions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public Collection<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(Collection<Condition> conditions) {
		this.conditions = conditions;
	}

}
