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
public class Group_ extends Persistable {

	@Column(length = 64)
	private String name;
	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, targetEntity = User_.class)
	private List<User_> users;
	@ManyToMany(mappedBy = "groups", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, targetEntity = Role_.class)
	private List<Role_> roles;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<User_> getUsers() {
		return users;
	}

	public void setUsers(List<User_> user_s) {
		this.users = user_s;
	}

	public List<Role_> getRoles() {
		return roles;
	}

	public void setRoles(List<Role_> role_s) {
		this.roles = role_s;
	}

}
