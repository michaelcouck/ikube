package ikube.model.security;

import ikube.model.Persistable;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;

@Entity
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Role extends Persistable {

	@Column(length = 64)
	private String access;
	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, targetEntity = User.class)
	private List<User> users;
	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, targetEntity = Group_.class)
	private List<Group_> groups;

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public List<Group_> getGroups() {
		return groups;
	}

	public void setGroups(List<Group_> groups) {
		this.groups = groups;
	}

}
