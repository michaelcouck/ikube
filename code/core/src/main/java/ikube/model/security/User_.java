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
public class User_ extends Persistable {

	@Column(length = 64)
	private String name;
	@Column(length = 64)
	private String password;
	@ManyToMany(mappedBy = "users", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, targetEntity = Role_.class)
	private List<Role_> roles;
	@ManyToMany(mappedBy = "users", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, targetEntity = Group_.class)
	private List<Group_> groups;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Role_> getRoles() {
		return roles;
	}

	public void setRoles(List<Role_> role_s) {
		this.roles = role_s;
	}

	public List<Group_> getGroups() {
		return groups;
	}

	public void setGroups(List<Group_> groups) {
		this.groups = groups;
	}

}
