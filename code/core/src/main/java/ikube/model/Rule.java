package ikube.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Michael Couck
 * @since 21.07.11
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = { @NamedQuery(name = Rule.SELECT_FROM_RULES, query = Rule.SELECT_FROM_RULES) })
public class Rule extends Persistable {

	public static final String SELECT_FROM_RULES = "select r from Rule as r";

	@Column(length = 64)
	private String name;
	@Column()
	private boolean result;
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	private Action action;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

}