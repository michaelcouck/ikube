package ikube.deploy.model;

import ikube.deploy.action.IAction;
import net.schmizz.sshj.SSHClient;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collection;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-06-2013
 */
public class Server {

	private transient SSHClient sshExec;

	private String ip;

	private String name;
	private String username;
	private String password;

	private Collection<IAction> actions;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Collection<IAction> getActions() {
		return actions;
	}

	public void setActions(Collection<IAction> actions) {
		this.actions = actions;
	}

	public SSHClient getSshExec() {
		return sshExec;
	}

	public void setSshExec(SSHClient sshExec) {
		this.sshExec = sshExec;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}